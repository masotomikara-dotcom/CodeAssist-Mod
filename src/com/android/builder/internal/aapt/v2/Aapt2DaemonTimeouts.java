package com.android.builder.internal.aapt.v2;
public class Aapt2DaemonTimeouts {}
EOF

cat > src/com/android/builder/internal/aapt/v2/AaptConvertConfig.java << 'JAVA'
package com.android.builder.internal.aapt.v2;
public class AaptConvertConfig {}
EOF

cat > src/com/android/builder/internal/aapt/v2/Aapt2Daemon.java << 'JAVA'
package com.android.builder.internal.aapt.v2;
import com.android.utils.ILogger;
public abstract class Aapt2Daemon {
    protected Aapt2Daemon(String displayId, ILogger logger) {}
    protected abstract void stopProcess();
    public abstract void doConvert(AaptConvertConfig aaptConvertConfig, ILogger logger);
}
EOF

# 4. Write the final 7.2.2-compliant implementation layout
cat > src/com/android/builder/internal/aapt/v2/Aapt2DaemonImpl.java << 'JAVA'
package com.android.builder.internal.aapt.v2;

import com.android.utils.ILogger;
import java.io.File;
import java.nio.file.Path;

/**
 * Robust structural patch for Aapt2DaemonImpl to preserve 4-parameter constructor
 * required by AarResourcesCompilerTransform reflection calls.
 */
public class Aapt2DaemonImpl extends Aapt2Daemon {

    // 4-Parameter Constructor invoked by AarResourcesCompilerTransform
    public Aapt2DaemonImpl(
            String aapt2ExecutablePath,
            Path daemonLog,
            Aapt2DaemonTimeouts timeouts,
            ILogger logger) {
        super("Aapt2Daemon", logger);
    }

    // 5-Parameter Constructor invoked by Aapt2DaemonManager
    public Aapt2DaemonImpl(
            String displayId,
            String aapt2ExecutablePath,
            Path daemonLog,
            Aapt2DaemonTimeouts timeouts,
            ILogger logger) {
        super(displayId, logger);
    }

    @Override
    protected void stopProcess() {
        // Safe lifecycle fallback stub
    }

    @Override
    public void doConvert(AaptConvertConfig aaptConvertConfig, ILogger logger) {
        // Safe compilation verification stub
    }
}
