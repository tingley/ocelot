package com.vistatec.ocelot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vistatec.ocelot.events.LQIDeselectionEvent;
import com.vistatec.ocelot.events.LQISelectionEvent;
import com.vistatec.ocelot.events.SegmentEditEvent;
import com.vistatec.ocelot.events.SegmentSelectionEvent;
import com.vistatec.ocelot.its.LanguageQualityIssue;
import com.vistatec.ocelot.its.NewLanguageQualityIssueView;
import com.vistatec.ocelot.segment.OcelotSegment;

public class SegmentMenu {
    private JMenu menu;
    private JMenuItem menuAddIssue, menuRemoveIssue, menuRestoreTarget;
    private OcelotSegment selectedSegment;
    private LanguageQualityIssue selectedLQI;

    private NewLanguageQualityIssueView addLQIView = null;

    public SegmentMenu(EventBus eventBus, int platformKeyMask) {
        menu = new JMenu("Segment");
        menuAddIssue = new JMenuItem("Add Issue");
        menuAddIssue.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, platformKeyMask));
        menuAddIssue.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                /**
                 * This is a gross workaround for a bizarre bug in the 1.7 Mac
                 * JRE, which results in the VK_EQUALS key event being sent
                 * 3 separate times. In order to prevent 3 separate dialogs
                 * from opening, we trap the extraneous events.
                 * This seems to be fixed in the 1.8 runtime.  See OC-41 for more.
                 */
                if (addLQIView == null) {
                    addLQIView = new NewLanguageQualityIssueView();
                    addLQIView.setWindowListener(new AddLQIViewWindowListener());
                    addLQIView.setSegment(selectedSegment);
                    SwingUtilities.invokeLater(addLQIView);
                }
            }
        });
        menuAddIssue.setEnabled(false);
        menu.add(menuAddIssue);
        menuRemoveIssue = new JMenuItem("Remove Issue");
        menuRemoveIssue.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                selectedSegment.removeLQI(selectedLQI);
            }
        });
        menuRemoveIssue.setEnabled(false);
        menu.add(menuRemoveIssue);
        menuRestoreTarget = new JMenuItem("Reset Target");
        menuRestoreTarget.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                selectedSegment.resetTarget();
                menuRestoreTarget.setEnabled(false);
            }
        });
        menuRestoreTarget.setEnabled(false);
        menu.add(menuRestoreTarget);
        eventBus.register(this);
    }

    class AddLQIViewWindowListener extends WindowAdapter {
        @Override
        public void windowClosed(WindowEvent e) {
            addLQIView = null;
        }
    }

    @Subscribe
    public void selectedSegment(SegmentSelectionEvent e) {
        menuAddIssue.setEnabled(true);
        menuRemoveIssue.setEnabled(false);
        menuRestoreTarget.setEnabled(e.getSegment().hasOriginalTarget());
        this.selectedSegment = e.getSegment();
    }

    @Subscribe
    public void segmentEdited(SegmentEditEvent e) {
        OcelotSegment seg = e.getSegment();
        if (seg.equals(selectedSegment)) {
            menuRestoreTarget.setEnabled(seg.hasOriginalTarget());
        }
    }

    @Subscribe
    public void selectedLQI(LQISelectionEvent e) {
        menuRemoveIssue.setEnabled(e.getLQI() != null);
        this.selectedLQI = e.getLQI();
    }

    @Subscribe
    public void deselectedLQI(LQIDeselectionEvent e) {
        selectedLQI = null;
        menuRemoveIssue.setEnabled(false);
    }

    public JMenu getMenu() {
        return menu;
    }
}
