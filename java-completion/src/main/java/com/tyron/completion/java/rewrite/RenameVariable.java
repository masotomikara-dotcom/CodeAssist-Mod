package com.tyron.completion.java.rewrite;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.tyron.completion.java.compiler.CompilerProvider;
import com.tyron.completion.java.util.FindCurrentPath;
import org.eclipse.lsp4j.Range;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class RenameVariable {

    private final CompilerProvider mCompiler;
    private final File mFile;
    private final int mCursorPosition;

    public RenameVariable(CompilerProvider compiler, File file, int cursorPosition) {
        this.mCompiler = compiler;
        this.mFile = file;
        this.mCursorPosition = cursorPosition;
    }

    public Optional<Range> getRenameRange() {
        if (mCompiler == null || mFile == null) {
            return Optional.empty();
        }
        TreePath currentPath = FindCurrentPath.findTargetAt(mCompiler.getTreeUtilities(), mFile, mCursorPosition);
        if (currentPath == null) {
            return Optional.empty();
        }

        Path mFilePath = mFile.toPath();

        if (currentPath.getLeaf() instanceof IdentifierTree) {
            IdentifierTree node = (IdentifierTree) currentPath.getLeaf();
            Range range = mCompiler.getTreeUtilities().getRangeOfNode(mFilePath, node);
            return Optional.ofNullable(range);
        }

        if (currentPath.getLeaf() instanceof VariableTree) {
            VariableTree node = (VariableTree) currentPath.getLeaf();
            Range range = mCompiler.getTreeUtilities().getRangeOfVariableName(mFilePath, node);
            return Optional.ofNullable(range);
        }

        return Optional.empty();
    }
}
