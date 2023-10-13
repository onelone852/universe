package path;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public final class GalaxyTrie {
    
    private Object prefix;
    private final HashMap<String, GalaxyTrie> map;

    public GalaxyTrie() {
        prefix = null;
        map = new HashMap<>();
    }

    public Object searchAndReplace(List<String> paths, Supplier<Object> supplier) {
        Iterator<String> paths_iter = paths.iterator();
        GalaxyTrie trie = this;
        while (true) {
            if (!paths_iter.hasNext()) {
                if (trie.prefix == null) {
                    trie.prefix = supplier.get();
                }
                return trie.prefix;
            }
            final String next_path = paths_iter.next();
            GalaxyTrie next_trie = trie.map.getOrDefault(next_path, null);
            if (next_trie == null) {
                next_trie = new GalaxyTrie();
                trie.map.put(next_path, next_trie);
            }
            trie = next_trie;
        }
    }
}
