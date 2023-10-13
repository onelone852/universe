package universe;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import passport.Meteor;
import passport.Passport;
import passport.Passport.PassportType;
import path.GalaxyTrie;
import path.PartialPath;
import path.PathsTrie;
import path.Planet;
import path.PartialPath.PartialPathsWithParams;
import path.PathsTrie.InsertWithPreinsertedKeyException;
import path.PathsTrie.ProcessorWithPathParam;
import procotol.DoorWay;
import procotol.ResponseStream;
import procotol.Status;
import procotol.Version;
import sync.Channel;
import sync.UnlimitedChannel;
import sync.WaitGroup;
import sync.WriteOnlyChannel;

public final class Universe implements AutoCloseable {

    private final ServerSocket socket;
    private final ExecutorService exec;
    private final boolean shutdownWhenEncounterError;
    private final InputStream commandListener;
    private final Status defaultStatus;

    /**
     * Create a new Universe. 
     * This is not intended to use this constructor directly.
     * Using UniverseBuilder is preferred instead of using this constructor directly.
     * 
     * @param addr
     * @param port
     * @param service
     * @param shutdownWhenEncounterError
     * @param commandListener
     * @param defaultStatus
     * @throws IOException
     * @see UniverseBuilder
     */
    public Universe(
        final String addr, 
        final int port, 
        final ExecutorService service, 
        final boolean shutdownWhenEncounterError,
        final InputStream commandListener,
        final Status defaultStatus
    ) throws IOException {
        socket = new ServerSocket();
        socket.bind(new InetSocketAddress(addr, port));
        exec = service;
        this.shutdownWhenEncounterError = shutdownWhenEncounterError;
        this.commandListener = commandListener;
        this.defaultStatus = defaultStatus;
    }

    private void listen_conn(final WriteOnlyChannel<Object> ch) {
        final Thread thread = new Thread(new Thread(() -> {
            while (true) {
                try {
                    final Socket conn = socket.accept();
                    ch.send(conn);
                } catch (Exception e) {
                    ch.send(e);
                    break;
                }
            }
        }));
        thread.setDaemon(true);
        thread.setName("Connection Listener");

        exec.submit(thread);
    }

    private void listen_command(final WriteOnlyChannel<Object> ch) {
        final Thread thread = new Thread(() -> {
            try {
                try (Scanner in = new Scanner(commandListener)) {
                    while (true) {
                        final String command = in.nextLine();
                        if (command.equals("shutdown")) {
                            ch.send(new ShutdownException("User call shutdown command."));
                            break;
                        }
                    } 
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        });
        thread.setDaemon(true);
        thread.setName("Command Listener");
        
        exec.submit(thread);
    }

    private static record Router(HashMap<DoorWay, PathsTrie> route, Planet defaultProcessor) {}

    private Router buildRouter(final List<Class<?>> simulatorList) throws InsertWithPreinsertedKeyException {
        final HashMap<DoorWay, PathsTrie> router = new HashMap<>();
        PathsTrie wholeGetPaths = new PathsTrie();
        PathsTrie wholePostPaths = new PathsTrie();
        PathsTrie wholePutPaths = new PathsTrie();
        PathsTrie wholeDeletePaths = new PathsTrie();
        Planet defaultProcessor = null;
        for (Class<?> simulator : simulatorList) {
            final passport.Galaxy galaxy = simulator.getAnnotation(passport.Galaxy.class);
            if (galaxy == null) {
                continue;
            }
            final List<PartialPath> galaxyPath = PartialPath.parse(galaxy.value()).paths();
            final PathsTrie getPaths = wholeGetPaths.createPathAndGet(galaxyPath.iterator());
            final PathsTrie postPaths = wholePostPaths.createPathAndGet(galaxyPath.iterator());
            final PathsTrie putPaths = wholePutPaths.createPathAndGet(galaxyPath.iterator());
            final PathsTrie deletePaths = wholeDeletePaths.createPathAndGet(galaxyPath.iterator()); // TODO: Make these calls lazy
            for (Method func_method : simulator.getMethods()) {
                if (func_method.isAnnotationPresent(passport.Planet.class)) {
                    List<PassportType> passports = PassportType.buildPassportTypes(func_method.getAnnotatedParameterTypes());
                    // Get the processing router and add them into PathsTrie
                    DoorWay.Get[] gets = func_method.getAnnotationsByType(DoorWay.Get.class);
                    for (DoorWay.Get get : gets) {
                        PartialPathsWithParams partialPaths = PartialPath.parse(get.value());
                        getPaths.insert(partialPaths.paths().iterator(), new Planet(func_method, passports, partialPaths.params()));
                    }

                    DoorWay.Post[] posts = func_method.getAnnotationsByType(DoorWay.Post.class);
                    for (DoorWay.Post post : posts) {
                        PartialPathsWithParams partialPaths = PartialPath.parse(post.value());
                        postPaths.insert(partialPaths.paths().iterator(), new Planet(func_method, passports, partialPaths.params()));
                    }

                    DoorWay.Put[] puts = func_method.getAnnotationsByType(DoorWay.Put.class);
                    for (DoorWay.Put put : puts) {
                        PartialPathsWithParams partialPaths = PartialPath.parse(put.value());
                        putPaths.insert(partialPaths.paths().iterator(), new Planet(func_method, passports, partialPaths.params()));
                    }

                    DoorWay.Delete[] deletes = func_method.getAnnotationsByType(DoorWay.Delete.class);
                    for (DoorWay.Delete delete : deletes) {
                        PartialPathsWithParams partialPaths = PartialPath.parse(delete.value());
                        deletePaths.insert(partialPaths.paths().iterator(), new Planet(func_method, passports, partialPaths.params()));
                    }
                    // TODO: Find a way to refactor this ugly stuff 
                } else if (func_method.isAnnotationPresent(Meteor.class)) {
                    List<Passport.PassportType> passports = PassportType.buildPassportTypes(func_method.getAnnotatedParameterTypes());
                    defaultProcessor = new Planet(func_method, passports, List.of());
                }
            }
        }
        
        router.put(DoorWay.GET, wholeGetPaths);
        router.put(DoorWay.POST, wholePostPaths);
        router.put(DoorWay.PUT, wholePutPaths);
        router.put(DoorWay.DELETE, wholeDeletePaths);
        return new Router(router, defaultProcessor);
    }

    private static record HandleGalaxy(Constructor<?> constructor, List<PassportType> passports) {}

    private static HandleGalaxy getHandleGalaxyConstructor(final Class<?> handleGalaxy) {
        for (Constructor<?> constructor : handleGalaxy.getConstructors()) {
            try {
                final List<PassportType> types = PassportType.buildPassportTypes(constructor.getAnnotatedParameterTypes());
                return new HandleGalaxy(constructor, types);
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }

    private static final class ResponseThreadFactory {

        private final WaitGroup wg;
        private final Channel<Object> ch;
        private final Router router;
        private final GalaxyTrie builtGalaxies;
        private final Status defaultStatus;

        public ResponseThreadFactory(final WaitGroup wg, final Channel<Object> ch, final Router router, final GalaxyTrie builtGalaxies, final Status defaultStatus) {
            this.wg = wg;
            this.ch = ch;
            this.router = router;
            this.builtGalaxies = builtGalaxies;
            this.defaultStatus = defaultStatus;
        }

        private String readInput(final InputStream in) {
            try {
                final byte[] bytes = new byte[in.available()];
                in.read(bytes);
                return new String(bytes, StandardCharsets.US_ASCII);
            } catch (Exception e) {
                return "";
            }
        }

        public Thread getReponseThread(final Socket conn) {
            return new Thread(() -> {wg.Work(); try {
                final InputStream in = conn.getInputStream();
                final PrintStream out = new PrintStream(conn.getOutputStream());

                String input = readInput(in);
                
                while (input.length() == 0) {
                    input = readInput(in);
                }

                final String[] statements = input.split("\r\n");
                final String requestStmt = statements[0];

                final String[] reqOp = requestStmt.split(" ");
                final DoorWay doorway = DoorWay.parse(reqOp[0]);
                final String route = reqOp[1];
                final Version httpVersion = Version.parse(reqOp[2]);
                
                final List<String> paths = Arrays.stream(route.split("/"))
                .filter((final String s) -> !s.isBlank())
                .toList();

                final ProcessorWithPathParam processorWithPathParam = router
                .route
                .get(doorway)
                .search(paths.iterator());
                
                final StringBuilder res = new StringBuilder();
                final ResponseStream rs = new ResponseStream(new Lock(), res, defaultStatus);

                if (!Objects.isNull(processorWithPathParam)) {
                    final Planet planet = processorWithPathParam.processor();
                    final Method processor = planet.method();

                    final Class<?> handleGalaxy = processor.getDeclaringClass();
                    final String modularPath = handleGalaxy.getAnnotation(passport.Galaxy.class).value();
                    final int galaxyRouteAmount = (int) Arrays.stream(modularPath
                    .split("/"))
                    .filter((final String s) -> !s.isBlank())
                    .count();

                    final List<Object> pathParamsWithGalaxyRoute = processorWithPathParam.params();
                    final List<Object> pathParams = pathParamsWithGalaxyRoute.subList(galaxyRouteAmount, pathParamsWithGalaxyRoute.size());

                    final Function<PassportType, Object> buildPassportParams = (final PassportType pt) -> {
                        if (pt instanceof PassportType.DoorWay) {
                            return doorway;
                        } else if (pt instanceof PassportType.Observable) {
                            return new ObservableUniverse(new Lock(), ch.toWriteOnlyChannel());
                        } else if (pt instanceof PassportType.Waiter) {
                            return wg;
                        } else if (pt instanceof PassportType.ResponseWriter) {
                            return rs;
                        } else if (pt instanceof PassportType.PathParameter) {
                            final PassportType.PathParameter pp = (PassportType.PathParameter) pt;
                            try {
                                return pathParams.get(planet.pathParams().indexOf(pp.name()));
                            } catch (ArrayIndexOutOfBoundsException e) {
                                return null;
                            }
                        } else if (pt instanceof PassportType.AccessVersion) {
                            return httpVersion;
                        }
                        return null;
                    };

                    final Object[] invokeObjects = planet
                    .param()
                    .stream()
                    .map(buildPassportParams)
                    .toArray();
                    
                    final Object galaxy;
                    if (Modifier.isStatic(processor.getModifiers())) {
                        galaxy = null;
                    } else {

                        final List<String> galaxyPath = paths.subList(0, galaxyRouteAmount);
                        
                        galaxy = builtGalaxies.searchAndReplace(galaxyPath, () -> {
                            final PartialPathsWithParams galaxyPathsWithParams = PartialPath.parse(modularPath);
                            final List<Object> galaxyParametersList = pathParamsWithGalaxyRoute.subList(0, galaxyRouteAmount);
                            final HandleGalaxy handleGalaxyConstructor = getHandleGalaxyConstructor(handleGalaxy);
                            try {
                                final Object[] initParams = handleGalaxyConstructor.passports.stream().map((final PassportType pt) -> {
                                    if (pt instanceof PassportType.DoorWay) {
                                        return doorway;
                                    } else if (pt instanceof PassportType.Observable) {
                                        return new ObservableUniverse(new Lock(), ch.toWriteOnlyChannel());
                                    } else if (pt instanceof PassportType.Waiter) {
                                        return wg;
                                    } else if (pt instanceof PassportType.ResponseWriter) {
                                        return rs;
                                    } else if (pt instanceof PassportType.PathParameter) {
                                        final PassportType.PathParameter pp = (PassportType.PathParameter) pt;
                                        try {
                                            return galaxyParametersList.get(galaxyPathsWithParams.params().indexOf(pp.name()));
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            return null;
                                        }
                                    } else if (pt instanceof PassportType.AccessVersion) {
                                        return httpVersion;
                                    }
                                    return null;
                                }).toArray();
                                return handleGalaxyConstructor.constructor.newInstance(initParams);
                            } catch (Exception e) {
                                ch.send(e);
                            }
                            return null;
                        });
                    } 
                    processor.invoke(galaxy, invokeObjects); 
                    Status status = rs.getStatus();
                    Version version = rs.getVersion();
                    out.printf("%s %s %s\r\n\r\n%s", version, status.code, status.codeName, res.toString());
                } else {
                    throw new NoMatchedPlanetException(doorway, route);
                }        
                // TODO: Find a way to refactor this stuff        
            } catch (InvocationTargetException e) {
                ch.send(e.getCause());
            } catch (Exception e) {
                ch.send(e);
            } finally {
                wg.Done();
                try {
                    conn.close();
                } catch (Exception e) {
                    ch.send(e);
                }
            }});
        }

        // TODO: Refactor this whole thing
    }   

    public Throwable simulate(Class<?> galaxy) throws IOException, InterruptedException, InsertWithPreinsertedKeyException {
        return simulate(Arrays.asList(galaxy.getDeclaredClasses()));
    }

    public Throwable simulate(List<Class<?>> simulators) throws IOException, InterruptedException, InsertWithPreinsertedKeyException {
        Throwable thrower = null;
        
        final WaitGroup wg = new WaitGroup();
        final Channel<Object> ch = new UnlimitedChannel<>();
        final Router router = buildRouter(simulators);
        final GalaxyTrie builtGalaxies = new GalaxyTrie();
        final ResponseThreadFactory factory = new ResponseThreadFactory(wg, ch, router, builtGalaxies, defaultStatus);

        listen_conn(ch.toWriteOnlyChannel());
        listen_command(ch.toWriteOnlyChannel());

        System.out.println("Universe - Start:\n  Start simulating universe in %s...".formatted(socket.getLocalSocketAddress().toString().substring(1)));

        loop: for (final Object sendObject : ch) {

            if (sendObject instanceof Socket) {
                final Socket conn = (Socket) sendObject;
                final Thread run_thread = factory.getReponseThread(conn);
                run_thread.setDaemon(true);
                exec.submit(run_thread);
            } else if (sendObject instanceof Throwable) {
                if (shutdownWhenEncounterError || sendObject instanceof ShutdownException) {
                    System.out.printf("Universe - Execption: \n  %s\n", sendObject);
                    thrower = (Throwable) sendObject;
                    break loop;
                } else {
                    final Thread printer = new Thread(() -> System.out.printf("Universe - Execption: \n  %s\n", sendObject));
                    printer.setDaemon(true);
                    printer.setName("Error printer");
                    exec.submit(printer);
                }
            } else {}
        }
        
        // * Cleanup
        System.out.println("Universe - Exit: Waiting for threads to be done...");
        wg.Wait();
        System.out.println("Universe - Exit: Exiting...");
        return thrower;
    }

    @Override
    public void close() throws IOException {
        socket.close();
        exec.shutdown();
    }

    public static class Lock {
        private Lock() {}
    }
}


