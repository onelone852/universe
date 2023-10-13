package path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

public final class PathsTrie {

    private Planet prefix;
    private final TreeMap<PartialPath, PathsTrie> map;

    public static record ProcessorWithPathParam(Planet processor, List<Object> params) {}

    private static final List<Class<? extends PartialPath>> classes = Arrays.asList(FixedPath.class, IntPath.class, AnyPath.class);

    private static class PathComparator implements Comparator<PartialPath> {

        List<Class<? extends PartialPath>> classes;

        public PathComparator(final List<Class<? extends PartialPath>> classes) {
            this.classes = classes;
        }

        @Override
        public int compare(final PartialPath p1, final PartialPath p2) {
            int path1 = classes.indexOf(p1.getClass());
            int path2 = classes.indexOf(p2.getClass());
            if (path1 < path2) {
                return -1;
            } else if (path1 > path2) {
                return 1;
            } else {
                if (p1.equals(p2)) {
                    return 0;
                }
                return -1;
            }
        }

    }

    public PathsTrie() {
        prefix = null;
        map = new TreeMap<>(new PathComparator(classes));
    }  

    public static class InsertWithPreinsertedKeyException extends Exception {}

    public PathsTrie createPathAndGet(final Iterator<PartialPath> paths_iter) throws InsertWithPreinsertedKeyException {
        PathsTrie trie = this;
        while (true) {
            if (!paths_iter.hasNext()) {
                return trie;
            }
            final PartialPath next_path = paths_iter.next();
            PathsTrie next_trie = trie.map.getOrDefault(next_path, null);
            if (next_trie == null) {
                next_trie = new PathsTrie();
                trie.map.put(next_path, next_trie);
            }
            trie = next_trie;
        }
    }

    public void insert(final Iterator<PartialPath> paths_iter, final Planet prefix) throws InsertWithPreinsertedKeyException {
        PathsTrie trie = this;
        while (true) {
            if (!paths_iter.hasNext()) {
                if (trie.prefix != null) {
                    throw new InsertWithPreinsertedKeyException();
                }
                trie.prefix = prefix;
                return;
            }
            final PartialPath next_path = paths_iter.next();
            PathsTrie next_trie = trie.map.getOrDefault(next_path, null);
            if (next_trie == null) {
                next_trie = new PathsTrie();
                trie.map.put(next_path, next_trie);
            }
            trie = next_trie;
        }
    }

    public ProcessorWithPathParam search(final Iterator<String> paths_iter) {
        final ArrayList<Object> objs = new ArrayList<>();
        PathsTrie trie = this;
        out: while (true) {
            if (!paths_iter.hasNext()) {
                if (trie.prefix == null) {
                    return null;
                }
                return new ProcessorWithPathParam(trie.prefix, objs);
            }
            Iterator<Map.Entry<PartialPath, PathsTrie>> iterator = trie.map.entrySet().iterator();
            final String next_path = paths_iter.next();
            while (iterator.hasNext()) {
                final Map.Entry<PartialPath, PathsTrie> entry = iterator.next();
                final Object obj = entry.getKey().match(next_path);
                if (obj != null) {
                    if (!(obj instanceof Null)) {
                        objs.add(obj);
                    }
                    trie = entry.getValue();
                    continue out;
                }
            }
            return null;
        }
        
    }

}
