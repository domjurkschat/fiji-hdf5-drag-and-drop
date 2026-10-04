// Drag and Drop HDF5 for Fiji
// https://github.com/domjurkschat/fiji-hdf5-drag-and-drop
// BSD-3-Clause license
import ij.IJ
import ij.Prefs
import ij.gui.GUI
import ij.gui.GenericDialog
import ij.plugin.FolderOpener
import java.awt.Button
import java.awt.Component
import java.awt.Container
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.event.ActionListener
import javax.swing.SwingUtilities

// ---------------- configuration ----------------
COMMAND = "HDF5/N5/Zarr/OME-NGFF ... "   // from the macro recorder
EXTENSIONS = ["h5", "hdf5", "hdf"]
DS_KEY = "hdf5dropwindow.dataset"        // persistent default dataset name
// -----------------------------------------------

// Takes drops containing HDF5 files; hands everything else to the original (stock) handler untouched
class HdfDropTarget extends DropTarget {
    Closure onFiles
    Closure hasHdf5
    DropTarget previous

    void dragEnter(DropTargetDragEvent e) {
        if (previous != null) previous.dragEnter(e) else e.acceptDrag(DnDConstants.ACTION_COPY)
    }

    void dragOver(DropTargetDragEvent e) {
        if (previous != null) previous.dragOver(e) else e.acceptDrag(DnDConstants.ACTION_COPY)
    }

    void dragExit(DropTargetEvent e) {
        if (previous != null) previous.dragExit(e)
    }

    void dropActionChanged(DropTargetDragEvent e) {
        if (previous != null) previous.dropActionChanged(e)
    }

    void drop(DropTargetDropEvent e) {
        if (!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            if (previous != null) previous.drop(e) else e.rejectDrop()
            return
        }
        e.acceptDrop(DnDConstants.ACTION_COPY)
        def files = e.transferable.getTransferData(DataFlavor.javaFileListFlavor)
        if (hasHdf5.call(files) || previous == null) {
            e.dropComplete(true)
            onFiles.call(files)
        } else {
            // Stock re-accepts this same event (legal while the drop is accepted) and completes it
            previous.drop(e)
        }
    }
}

// GenericDialog builds its OK/Cancel row inside showDialog(), so inject our button
// as that row appears: index 0 places it left of OK
class DatasetDialog extends GenericDialog {
    Closure onSetDefault

    DatasetDialog(String title) { super(title) }

    void setVisible(boolean visible) {
        if (visible) {
            def parent = getButtons()[0]?.getParent()
            if (parent != null) {
                def button = new Button("Set default")
                button.addActionListener({ onSetDefault.call(getStringFields()[0].text) } as ActionListener)
                parent.add(button, 0)
                pack()
                GUI.centerOnImageJScreen(this)
            }
        }
        super.setVisible(visible)
    }
}

def isHdf5 = { File f -> !f.isDirectory() && EXTENSIONS.any { f.name.toLowerCase().endsWith("." + it) } }
def hasHdf5 = { files -> files.any { isHdf5(it) } }

// If a drop target is one of ours (from an earlier run), use the original it wraps
def unwrap = { DropTarget d ->
    (d != null && d.getClass().simpleName == "HdfDropTarget") ? d.previous : d
}

// Directories must not go through IJ.open: in Fiji that is patched to the SciJava
// legacy opener, which has no I/O plugin for folders and logs "No appropriate format found"
def openOne = { File f ->
    try {
        if (f.isDirectory()) FolderOpener.open(f.path)
        else IJ.open(f.path)
    } catch (Throwable t) {
        IJ.log("Could not open " + f.name + ": " + t.getMessage())
    }
}

def askDatasetPath = { int n ->
    def gd = new DatasetDialog("Dataset path")
    gd.addStringField("Dataset path (" + n + " file(s))", Prefs.get(DS_KEY, "images"), 40)
    gd.onSetDefault = { String name ->
        name = name?.trim()
        if (name) {
            Prefs.set(DS_KEY, name)
            Prefs.savePreferences()
            IJ.showStatus("Default dataset: " + name)
        } else
            IJ.showStatus("Enter a dataset name first")
    }
    gd.showDialog()
    if (gd.wasCanceled()) return null
    def ds = gd.getNextString()?.trim()
    return ds ?: null
}

def handleDrop = { files ->
    new Thread({
        def hdf = files.findAll { isHdf5(it) }.sort { it.path }
        files.findAll { !isHdf5(it) }.each { openOne(it) }
        if (hdf.isEmpty()) return
        def ds = askDatasetPath(hdf.size())
        if (ds == null) return
        hdf.each { f ->
            try {
                IJ.run(COMMAND, "url=hdf5://" + f.toURI() + "?" + ds + " virtual")
            } catch (Throwable t) {
                IJ.log("Could not open " + f.name + ": " + t.getMessage())
            }
        }
    }).start()
}

def main = IJ.getInstance()

// Hook the main Fiji window (toolbar, status bar, etc.); children inherit the nearest original handler
if (main != null) {
    def hook
    hook = { Component c, DropTarget inherited ->
        def own = unwrap(c.getDropTarget()) ?: inherited
        c.setDropTarget(new HdfDropTarget(onFiles: handleDrop, hasHdf5: hasHdf5, previous: own))
        if (c instanceof Container) c.getComponents().each { hook(it, own) }
    }
    SwingUtilities.invokeLater { hook(main, null) }
}
