package chav1961.ksmgr.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

public class JHighlightedScrollPane extends JScrollPane {
	private static final long serialVersionUID = -40942004121680531L;

	public JHighlightedScrollPane(final Component view) {
		super(view);
		view.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				JHighlightedScrollPane.this.setBorder(new LineBorder(Color.BLACK));
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				JHighlightedScrollPane.this.setBorder(new LineBorder(Color.BLUE, 2));
			}
		});
	}
}
