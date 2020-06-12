package parts.code.interactive.queries

import parts.code.streams.StreamProcessingApplication
import ratpack.test.MainClassApplicationUnderTest

class ApplicationsUnderTest {
    final def instance = new MainClassApplicationUnderTest(StreamProcessingApplication)

    boolean started() {
        return (instance.address != null)
    }

    void close() {
        instance.close()
    }
}
