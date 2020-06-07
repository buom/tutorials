package parts.code.interactive.queries

import ratpack.impose.ForceServerListenPortImposition
import ratpack.impose.ImpositionsSpec
import ratpack.test.MainClassApplicationUnderTest

class ApplicationsUnderTest {
    final def instance1 = new MainClassApplicationUnderTest(Application) {
        void addImpositions(ImpositionsSpec impositions) {
            impositions.add(ForceServerListenPortImposition.of(5051))
        }
    }

    final def instance2 = new MainClassApplicationUnderTest(Application) {
        void addImpositions(ImpositionsSpec impositions) {
            impositions.add(ForceServerListenPortImposition.of(5052))
        }
    }

    boolean started() {
        return (instance1.address != null && instance2.address != null)
    }

    void close() {
        instance1.close()
        instance2.close()
    }
}
