package bitronix.tm.internal;

import java.util.*;

/**
 * Container of {@link XAResourceHolderState} ordering them by priorities. A {@link XAResourceHolderState}'s priority
 * depends on the value of the {@link XAResourceHolderState#getCommitOrderingPosition()}.
 * <p>&copy; Bitronix 2005, 2006, 2007, 2008</p>
 *
 * @author lorban
 */
public class ResourceScheduler {

    public static final int ALWAYS_FIRST_POSITION = Integer.MIN_VALUE;
    public static final int ALWAYS_LAST_POSITION = Integer.MAX_VALUE;
    public static final Object ALWAYS_FIRST_POSITION_KEY = new Integer(Integer.MIN_VALUE);
    public static final Object ALWAYS_LAST_POSITION_KEY = new Integer(Integer.MAX_VALUE);

    private Map resources = new TreeMap();

    
    public ResourceScheduler() {
    }

    public void addResource(XAResourceHolderState xaResourceHolderState) {
        int position = xaResourceHolderState.getCommitOrderingPosition();
        addAtPosition(xaResourceHolderState, position);
    }

    private void addAtPosition(XAResourceHolderState xaResourceHolderState, int position) {
        Integer key = new Integer(position);
        List resourcesList = (List) resources.get(key);
        if (resourcesList == null) {
            resourcesList = new ArrayList();
            resources.put(key, resourcesList);
        }
        resourcesList.add(xaResourceHolderState);
    }

    public SortedSet getNaturalOrderPriorities() {
        return new TreeSet(resources.keySet());
    }

    public SortedSet getReverseOrderPriorities() {
        TreeSet result = new TreeSet(Collections.reverseOrder());
        result.addAll(getNaturalOrderPriorities());
        return result;
    }

    public List getNaturalOrderResourcesForPriority(Object priorityKey) {
        return (List) resources.get(priorityKey);
    }

    public List getReverseOrderResourcesForPriority(Object priorityKey) {
        List result = new ArrayList(getNaturalOrderResourcesForPriority(priorityKey));
        Collections.reverse(result);
        return result;
    }

    public int size() {
        int totalSize = 0;
        Iterator it = resources.keySet().iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            List list = (List) resources.get(key);
            totalSize += list.size();
        }
        return totalSize;
    }

    public Iterator iterator() {
        return new ResourceSchedulerIterator(this);
    }

    public String toString() {
        return "a ResourceScheduler with " + size() + " resource(s) in " + getNaturalOrderPriorities().size() + " priority(ies)";
    }


    private class ResourceSchedulerIterator implements Iterator {
        private ResourceScheduler resourceScheduler;
        private Iterator resourceSchedulerKeysIterator;
        private List resourcesOfCurrentKey;
        private int resourcesOfCurrentKeyIndex;

        private ResourceSchedulerIterator(ResourceScheduler resourceScheduler) {
            this.resourceScheduler = resourceScheduler;
            resourceSchedulerKeysIterator = resourceScheduler.resources.keySet().iterator();
        }

        public void remove() {
        }

        public boolean hasNext() {
            if (resourcesOfCurrentKey == null || resourcesOfCurrentKeyIndex >= resourcesOfCurrentKey.size()) {
                if (resourceSchedulerKeysIterator.hasNext()) {
                    Integer currentKey = (Integer) resourceSchedulerKeysIterator.next();
                    resourcesOfCurrentKey = (List) resourceScheduler.resources.get(currentKey);
                    resourcesOfCurrentKeyIndex = 0;
                    return true;
                }
                return false;
            }
            return true;
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException("iterator bounds reached");
            return resourcesOfCurrentKey.get(resourcesOfCurrentKeyIndex++);
        }
    }

}