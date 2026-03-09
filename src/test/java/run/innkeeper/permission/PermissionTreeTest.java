package run.innkeeper.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTreeTest {

    private PermissionTree tree;

    @BeforeEach
    void setUp() {
        tree = new PermissionTree();
    }

    @Test
    void hasPerm_emptyTree_returnsFalse() {
        assertFalse(tree.hasPerm("some.perm"));
    }

    @Test
    void add_exactPermission_canBeChecked() {
        tree.add("admin.users.read");
        assertTrue(tree.hasPerm("admin.users.read"));
    }

    @Test
    void hasPerm_nonExistentPermission_returnsFalse() {
        tree.add("admin.users.read");
        assertFalse(tree.hasPerm("admin.users.write"));
    }

    @Test
    void add_wildcardStar_matchesDirect() {
        tree.add("admin.*");
        assertTrue(tree.hasPerm("admin.users"));
    }

    @Test
    void add_wildcardStar_doesNotMatchDeeper() {
        tree.add("admin.*");
        assertFalse(tree.hasPerm("admin.users.read"));
    }

    @Test
    void add_doubleWildcard_matchesAllSub() {
        tree.add("admin.**");
        assertTrue(tree.hasPerm("admin.users"));
        assertTrue(tree.hasPerm("admin.users.read"));
        assertTrue(tree.hasPerm("admin.users.read.more"));
    }

    @Test
    void add_multiplePermissions() {
        tree.add("admin.users.read");
        tree.add("admin.users.write");
        tree.add("guest.view");
        assertTrue(tree.hasPerm("admin.users.read"));
        assertTrue(tree.hasPerm("admin.users.write"));
        assertTrue(tree.hasPerm("guest.view"));
        assertFalse(tree.hasPerm("admin.users.delete"));
    }

    @Test
    void hasPerm_partialMatch_returnsFalse() {
        tree.add("admin.users.read");
        assertFalse(tree.hasPerm("admin.users"));
        assertFalse(tree.hasPerm("admin"));
    }

    @Test
    void getRoot_returnsRootNode() {
        assertNotNull(tree.getRoot());
    }

    @Test
    void rootNode_getters() {
        tree.add("admin.**");
        PermissionTree.Node root = tree.getRoot();
        assertNotNull(root.getNodes());
        assertFalse(root.isDirectSub());
        assertFalse(root.isAllSub());
        assertFalse(root.isHas());
    }

    @Test
    void add_singleSegment() {
        tree.add("admin");
        assertTrue(tree.hasPerm("admin"));
    }

    @Test
    void hasPerm_completelyWrongPath() {
        tree.add("admin.users.read");
        assertFalse(tree.hasPerm("guest.view"));
    }

    @Test
    void add_duplicatePermission_noError() {
        tree.add("admin.users");
        tree.add("admin.users");
        assertTrue(tree.hasPerm("admin.users"));
    }
}
