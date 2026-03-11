package run.innkeeper.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LatestCommitCheckTest {

    @Test
    void defaultConstructor_generatesName() {
        LatestCommitCheck lcc = new LatestCommitCheck();
        assertNotNull(lcc.getName());
        assertTrue(lcc.getName().startsWith("glc-"));
    }

    @Test
    void namedConstructor_usesGivenName() {
        LatestCommitCheck lcc = new LatestCommitCheck("testname");
        assertEquals("glc-testname", lcc.getName());
    }

    @Test
    void newInstance_withName() {
        LatestCommitCheck lcc = LatestCommitCheck.newInstance("myname");
        assertEquals("glc-myname", lcc.getName());
    }

    @Test
    void newInstance_withoutName() {
        LatestCommitCheck lcc = LatestCommitCheck.newInstance();
        assertNotNull(lcc.getName());
        assertTrue(lcc.getName().startsWith("glc-"));
    }

    @Test
    void hasCorrectImage() {
        LatestCommitCheck lcc = new LatestCommitCheck("test");
        assertEquals("ghcr.io/innkeeperdevops/git-latest:12", lcc.getImage());
    }

    @Test
    void hasDefaultEnvVars() {
        LatestCommitCheck lcc = new LatestCommitCheck("test");
        assertTrue(lcc.getEnVars().containsKey("GIT_REPO"));
    }
}
