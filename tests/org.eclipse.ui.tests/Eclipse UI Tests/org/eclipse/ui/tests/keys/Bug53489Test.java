/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.keys;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests that pressing delete in a styled text widget does not cause a double
 * delete situation.
 * 
 * @since 3.0
 */
public class Bug53489Test extends UITestCase {

    /**
     * Constructor for Bug53489Test.
     * 
     * @param name
     *            The name of the test
     */
    public Bug53489Test(String name) {
        super(name);
    }

    /**
     * Tests that pressing delete in a styled text widget (in a running
     * Eclipse) does not cause a double delete.
     * 
     * @throws AWTException
     *             If the creation of robot
     * @throws CommandException
     *             If execution of the handler fails.
     * @throws CoreException
     *             If the test project cannot be created and opened.
     * @throws IOException
     *             If the file cannot be read.
     */
    public void testDoubleDelete() throws AWTException, CommandException,
            CoreException, IOException {
        IWorkbenchWindow window = openTestWindow();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject testProject = workspace.getRoot().getProject(
                "DoubleDeleteestProject"); //$NON-NLS-1$
        testProject.create(null);
        testProject.open(null);
        IFile textFile = testProject.getFile("A.txt"); //$NON-NLS-1$
        String originalContents = "A blurb"; //$NON-NLS-1$
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                originalContents.getBytes());
        textFile.create(inputStream, true, null);
        IEditorPart editor = IDE.openEditor(window.getActivePage(), textFile, true);
        
        // Allow the editor to finish opening.
        Display display = Display.getCurrent();
        while (display.readAndDispatch())
            ;

        // Press Delete
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        // Spin the event loop.
        while (display.readAndDispatch())
            ;

        // Test the text is only one character different.
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(
                textFile.getContents()));
        String currentContents = reader.readLine();
        assertTrue("'DEL' deleted more than one key.", (originalContents //$NON-NLS-1$
                .length() == (currentContents.length() + 1)));
    }
}
