package com.tyron.completion.java.rewrite;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.tyron.builder.project.Project;
import com.tyron.completion.java.CompilerProvider;
import com.tyron.completion.java.action.FindCurrentPath;
import com.tyron.completion.model.JavaRewrite;
import com.tyron.completion.model.Range;
import com.tyron.completion.model.TextEdit;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameVariable implements JavaRewrite {
    
    private final Path mFilePath;
    private final int mCursorPosition;
    private final String mNewName;

    public RenameVariable(Path filePath, int cursorPosition, String newName) {
        this.mFilePath = filePath;
        this.mCursorPosition = cursorPosition;
        this.mNewName = newName;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        Map<Path, TextEdit[]> editsMap = new HashMap<>();
        List<TextEdit> edits = new ArrayList<>();

        File file = mFilePath.toFile();
        TreePath currentPath = FindCurrentPath.findTargetAt(compiler, file, mCursorPosition);
        if (currentPath == null) {
            return editsMap;
        }

        String targetName = null;
        TreePath scopePath = null;

        if (currentPath.getLeaf() instanceof VariableTree) {
            targetName = ((VariableTree) currentPath.getLeaf()).getName().toString();
            scopePath = currentPath.getParentPath();
        } else if (currentPath.getLeaf() instanceof IdentifierTree) {
            targetName = ((IdentifierTree) currentPath.getLeaf()).getName().toString();
            scopePath = currentPath.getParentPath();
            while (scopePath != null && !(scopePath.getLeaf() instanceof com.sun.source.tree.MethodTree) && !(scopePath.getLeaf() instanceof com.sun.source.tree.ClassTree)) {
                scopePath = scopePath.getParentPath();
            }
        }

        if (targetName == null || scopePath == null) {
            return editsMap;
        }

        final String finalTargetName = targetName;

        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (node.getName().toString().equals(finalTargetName)) {
                    Range range = compiler.getRangeOfNode(mFilePath, node);
                    if (range != null) {
                        edits.add(new TextEdit(range, mNewName));
                    }
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                if (node.getName().toString().equals(finalTargetName)) {
                    Range range = compiler.getRangeOfVariableName(mFilePath, node);
                    if (range != null) {
                        edits.add(new TextEdit(range, mNewName));
                    }
                }
                return super.visitVariable(node, p);
            }
        }.scan(scopePath, null);

        if (!edits.isEmpty()) {
            editsMap.put(mFilePath, edits.toArray(new TextEdit[0]));
        }

        return editsMap;
    }
}