package run.innkeeper.permission;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PermissionTree implements Serializable{
  class Node implements Serializable{
    boolean directSub;
    boolean allSub;
    boolean has;
    Map<String, Node> nodes = new HashMap<>();

    public Node() {

    }

    public Node upsert(String word) {
      if (word.equals("**")) {
        this.allSub = true;
        return null;
      }
      if (word.equals("*")) {
        this.directSub = true;
        return null;
      }
      Node n = nodes.get(word);
      if (n != null) {
        return n;
      }
      Node newNode = new Node();
      nodes.put(word, newNode);
      return newNode;
    }

    public boolean hasPerm(Queue<String> words) {
      if (words.size() == 0 && has)
        return true;
      if (words.size() == 1 && this.directSub) {
        return true;
      }
      if (words.size() >= 1 && this.allSub) {
        return true;
      }
      if (words.size() == 0)
        return false;
      Node n = nodes.get(words.remove());
      if (n != null) {
        return n.hasPerm(words);
      }
      return false;

    }

    public boolean isDirectSub() {
      return directSub;
    }

    public boolean isAllSub() {
      return allSub;
    }

    public boolean isHas() {
      return has;
    }

    public Map<String, Node> getNodes() {
      return nodes;
    }
  }

  Node root = new Node();

  public PermissionTree() {

  }

  public boolean hasPerm(String permission) {
    return root.hasPerm(new LinkedBlockingQueue<>(){{
      addAll(Arrays.asList(permission.split("\\.")));
    }});
  }

  public void add(String permissionString) {
    String[] items = permissionString.split("\\.");
    Node currentNode = root;
    for (int i = 0; i < items.length; i++) {
      currentNode = currentNode.upsert(items[i]);
    }
    if (currentNode != null)
      currentNode.has = true;
  }

  public Node getRoot() {
    return root;
  }
}
