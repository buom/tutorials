package parts.code.interactive.queries

import ratpack.test.MainClassApplicationUnderTest

class ApplicationsUnderTest {
    final def instance1 = new MainClassApplicationUnderTest(Application)

    boolean started() {
        return (instance1.address != null)
    }

    void close() {
        instance1.close()
    }
}
