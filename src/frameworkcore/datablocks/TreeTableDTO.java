package frameworkcore.datablocks;

import java.util.List;
import java.util.Map;

/**
 * @author Ashish Vishwakarma (ashish.b.vishwakarma)
 */
public class TreeTableDTO {

    private Map<String, List<String>> beforeWhereMap;
    private Map<String, List<String>> nodePathMap;
    private Map<String, List<String>> afterNodepathMap;

    /**
     * @return the beforeWhereMap
     */
    public Map<String, List<String>> getBeforeWhereMap() {
        return beforeWhereMap;
    }

    /**
     * @param beforeWhereMap the beforeWhereMap to set
     */
    public void setBeforeWhereMap(Map<String, List<String>> beforeWhereMap) {
        this.beforeWhereMap = beforeWhereMap;
    }

    /**
     * @return the nodePathMap
     */
    public Map<String, List<String>> getNodePathMap() {
        return nodePathMap;
    }

    /**
     * @param nodePathMap the nodePathMap to set
     */
    public void setNodePathMap(Map<String, List<String>> nodePathMap) {
        this.nodePathMap = nodePathMap;
    }

    /**
     * @return the afterNodepathMap
     */
    public Map<String, List<String>> getAfterNodepathMap() {
        return afterNodepathMap;
    }

    /**
     * @param afterNodepathMap the afterNodepathMap to set
     */
    public void setAfterNodepathMap(Map<String, List<String>> afterNodepathMap) {
        this.afterNodepathMap = afterNodepathMap;
    }

}
