package jams.juice.gui.tree;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public abstract class AbstractTreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener {

    private JAMSTree tree;
    private DragSource dragSource; // dragsource
    private DropTarget dropTarget; //droptarget
    private static JAMSNode draggedNode;
    private JAMSNode draggedNodeParent;
    private static JLabel draggedLabel;
    private static JLayeredPane dragPane;
    private boolean drawImage;

    protected AbstractTreeTransferHandler(JAMSTree tree, int action, boolean drawIcon) {
        this.tree = tree;
        drawImage = drawIcon;
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(tree, action, this);
        dropTarget = new DropTarget(tree, action, this);
    }

    /* Methods for DragSourceListener */
    public void dragDropEnd_(DragSourceDropEvent dsde) {
        if (drawImage) {
            dragPane.remove(draggedLabel);
            dragPane.repaint(draggedLabel.getBounds());
        }
        if (dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);
            tree.expandPath(new TreePath(draggedNodeParent.getPath()));
            tree.expandPath(new TreePath(draggedNode.getPath()));
        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {

        if (drawImage) {
            dragPane.remove(draggedLabel);
            dragPane.repaint(draggedLabel.getBounds());
        }
        if (dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);

            tree.expandPath(new TreePath(draggedNodeParent.getPath()));
            tree.updateUI();
            tree.restoreExpandedState();
        }
    }

    @Override
    public final void dragEnter(DragSourceDragEvent dsde) {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY) {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else {
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    @Override
    public final void dragOver(DragSourceDragEvent dsde) {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY) {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else {
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    @Override
    public final void dropActionChanged(DragSourceDragEvent dsde) {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY) {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else {
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    @Override
    public final void dragExit(DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /* Methods for DragGestureListener */
    @Override
    public final void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            draggedNode = (JAMSNode) path.getLastPathComponent();
            Vector<Boolean> expandedStates = new Vector<Boolean>();
            for (Enumeration enumeration = draggedNode.depthFirstEnumeration(); enumeration.hasMoreElements();) {
                JAMSNode element = (JAMSNode) enumeration.nextElement();
                TreePath treePath = new TreePath(element.getPath());
                expandedStates.add(new Boolean(tree.isExpanded(treePath)));
            }
            draggedNodeParent = (JAMSNode) draggedNode.getParent();
            BufferedImage image = null;
            if (drawImage) {
                Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
                JComponent lbl = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false, tree.isExpanded(path), ((DefaultTreeModel) tree.getModel()).isLeaf(path.getLastPathComponent()), 0, false); //returning the label
                lbl.setBounds(pathBounds); //setting bounds to lbl
                image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE); //buffered image reference passing the label's ht and width
                Graphics2D graphics = image.createGraphics(); //creating the graphics for buffered image
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); //Sets the Composite for the Graphics2D context
                lbl.setOpaque(false);
                lbl.paint(graphics);
                graphics.dispose();
                draggedLabel = new JLabel(new ImageIcon(image));
                draggedLabel.setOpaque(false);

                //draggedLabel.setBounds(pathBounds);
                draggedLabel.setSize(pathBounds.getSize());
                paintImage(pathBounds.getLocation(), dge.getComponent());

                Container container = tree.getTopLevelAncestor();
                if (container == null) {
                    drawImage = false;
                } else {
                    if (container instanceof JWindow) {
                        dragPane = ((JWindow) tree.getTopLevelAncestor()).getLayeredPane();
                        dragPane.add(draggedLabel, JLayeredPane.DRAG_LAYER);
                    } else if (container instanceof JFrame) {
                        dragPane = ((JFrame) tree.getTopLevelAncestor()).getLayeredPane();
                        dragPane.add(draggedLabel, JLayeredPane.DRAG_LAYER);
                    } else if (container instanceof JApplet) {
                        dragPane = ((JApplet) tree.getTopLevelAncestor()).getLayeredPane();
                        dragPane.add(draggedLabel, JLayeredPane.DRAG_LAYER);
                    } else {
                        drawImage = false;
                    }
                }
            }
            dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0), new TransferableNode(draggedNode, expandedStates), this);
        }
    }

    /* Methods for DropTargetListener */
    @Override
    public final void dragEnter(DropTargetDragEvent dtde) {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage) {
            paintImage(pt, ((DropTarget) dtde.getSource()).getComponent());
        }
        if (canPerformAction(tree, draggedNode, action, pt)) {
            dtde.acceptDrag(action);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void dragExit(DropTargetEvent dte) {
    }

    @Override
    public final void dragOver(DropTargetDragEvent dtde) {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        tree.autoscroll(pt);
        if (drawImage) {
            paintImage(pt, ((DropTarget) dtde.getSource()).getComponent());
        }
        if (canPerformAction(tree, draggedNode, action, pt)) {
            dtde.acceptDrag(action);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void dropActionChanged(DropTargetDragEvent dtde) {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage) {
            paintImage(pt, ((DropTarget) dtde.getSource()).getComponent());
        }
        if (canPerformAction(tree, draggedNode, action, pt)) {
            dtde.acceptDrag(action);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void drop(DropTargetDropEvent dtde) {
        try {
            int action = dtde.getDropAction();
            Transferable transferable = dtde.getTransferable();
            Point pt = dtde.getLocation();
            if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR) && canPerformAction(tree, draggedNode, action, pt)) {
                TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
                JAMSNode node = (JAMSNode) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
                JAMSNode newParentNode = (JAMSNode) pathTarget.getLastPathComponent();
                
                Vector expandedStates = (Vector) transferable.getTransferData(TransferableNode.EXPANDED_STATE_FLAVOR);
                if (executeDrop(tree, node, newParentNode, expandedStates, action)) {
                    dtde.acceptDrop(action);
                    dtde.dropComplete(true);
                    return;
                }
            }
            dtde.rejectDrop();
            dtde.dropComplete(false);
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
            dtde.dropComplete(false);
        }
    }

    private final synchronized void paintImage(Point pt, Component source) {
        pt = SwingUtilities.convertPoint(source, pt, dragPane);
        draggedLabel.setLocation(pt);
    }

    public abstract boolean canPerformAction(JAMSTree target, JAMSNode draggedNode, int action, Point location);

    public abstract boolean executeDrop(JAMSTree tree, JAMSNode draggedNode, JAMSNode newParentNode, Vector expandedStates, int action);
}
