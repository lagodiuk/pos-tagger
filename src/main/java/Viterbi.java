/*
 * Viterbi.java
 * Toy Viterbi Decorder
 *
 * by Yusuke Shinyama <yusuke at cs . nyu . edu>
 *
 *   Permission to use, copy, modify, distribute this software
 *   for any purpose is hereby granted without fee, provided
 *   that the above copyright notice appear in all copies and
 *   that both that copyright notice and this permission notice
 *   appear in supporting documentation.
 *
 *   http://stackoverflow.com/questions/15348351/viterbi-algorithm-in-java
 */

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

class Symbol {
	public String name;

	public Symbol(String s) {
		this.name = s;
	}
}

class SymbolTable {
	Hashtable table;

	public SymbolTable() {
		this.table = new Hashtable();
	}

	public Symbol intern(String s) {
		s = s.toLowerCase();
		Object sym = this.table.get(s);
		if (sym == null) {
			sym = new Symbol(s);
			this.table.put(s, sym);
		}
		return (Symbol) sym;
	}
}

class SymbolList {
	Vector list;

	public SymbolList() {
		this.list = new Vector();
	}

	public int size() {
		return this.list.size();
	}

	public void set(int index, Symbol sym) {
		this.list.setElementAt(sym, index);
	}

	public void add(Symbol sym) {
		this.list.addElement(sym);
	}

	public Symbol get(int index) {
		return (Symbol) this.list.elementAt(index);
	}
}

class IntegerList {
	Vector list;

	public IntegerList() {
		this.list = new Vector();
	}

	public int size() {
		return this.list.size();
	}

	public void set(int index, int i) {
		this.list.setElementAt(new Integer(i), index);
	}

	public void add(int i) {
		this.list.addElement(new Integer(i));
	}

	public int get(int index) {
		return ((Integer) this.list.elementAt(index)).intValue();
	}
}

class ProbTable {
	Hashtable table;

	public ProbTable() {
		this.table = new Hashtable();
	}

	public void put(Object obj, double prob) {
		this.table.put(obj, new Double(prob));
	}

	public double get(Object obj) {
		Double prob = (Double) this.table.get(obj);
		if (prob == null) {
			return 0.0;
		}
		return prob.doubleValue();
	}

	// normalize probability
	public void normalize() {
		double total = 0.0;
		for (Enumeration e = this.table.elements(); e.hasMoreElements();) {
			total += ((Double) e.nextElement()).doubleValue();
		}
		if (total == 0.0) {
			return; // div by zero!
		}
		for (Enumeration e = this.table.keys(); e.hasMoreElements();) {
			Object k = e.nextElement();
			double prob = ((Double) this.table.get(k)).doubleValue();
			this.table.put(k, new Double(prob / total));
		}
	}
}

class State {
	public String name;
	ProbTable emits;
	ProbTable linksto;

	public State(String s) {
		this.name = s;
		this.emits = new ProbTable();
		this.linksto = new ProbTable();
	}

	public void normalize() {
		this.emits.normalize();
		this.linksto.normalize();
	}

	public void addSymbol(Symbol sym, double prob) {
		this.emits.put(sym, prob);
	}

	public double emitprob(Symbol sym) {
		return this.emits.get(sym);
	}

	public void addLink(State st, double prob) {
		this.linksto.put(st, prob);
	}

	public double transprob(State st) {
		return this.linksto.get(st);
	}
}

class StateTable {
	Hashtable table;

	public StateTable() {
		this.table = new Hashtable();
	}

	public State get(String s) {
		s = s.toUpperCase();
		State st = (State) this.table.get(s);
		if (st == null) {
			st = new State(s);
			this.table.put(s, st);
		}
		return st;
	}
}

class StateIDTable {
	Hashtable table;

	public StateIDTable() {
		this.table = new Hashtable();
	}

	public void put(State obj, int i) {
		this.table.put(obj, new Integer(i));
	}

	public int get(State obj) {
		Integer i = (Integer) this.table.get(obj);
		if (i == null) {
			return 0;
		}
		return i.intValue();
	}
}

class StateList {
	Vector list;

	public StateList() {
		this.list = new Vector();
	}

	public int size() {
		return this.list.size();
	}

	public void set(int index, State st) {
		this.list.setElementAt(st, index);
	}

	public void add(State st) {
		this.list.addElement(st);
	}

	public State get(int index) {
		return (State) this.list.elementAt(index);
	}
}

class HMMCanvas extends Canvas {
	static final int grid_x = 60;
	static final int grid_y = 40;
	static final int offset_x = 70;
	static final int offset_y = 30;
	static final int offset_y2 = 10;
	static final int offset_y3 = 65;
	static final int col_x = 40;
	static final int col_y = 10;
	static final int state_r = 10;
	static final Color state_fill = Color.white;
	static final Color state_fill_maximum = Color.yellow;
	static final Color state_fill_best = Color.red;
	static final Color state_boundery = Color.black;
	static final Color link_normal = Color.green;
	static final Color link_processed = Color.blue;
	static final Color link_maximum = Color.red;

	HMMDecoder hmm;

	public HMMCanvas() {
		this.setBackground(Color.white);
		this.setSize(400, 300);
	}

	public void setHMM(HMMDecoder h) {
		this.hmm = h;
	}

	private void drawState(Graphics g, int x, int y, Color c) {
		x = (x * grid_x) + offset_x;
		y = (y * grid_y) + offset_y;
		g.setColor(c);
		g.fillOval(x - state_r, y - state_r, state_r * 2, state_r * 2);
		g.setColor(state_boundery);
		g.drawOval(x - state_r, y - state_r, state_r * 2, state_r * 2);
	}

	private void drawLink(Graphics g, int x, int y0, int y1, Color c) {
		int x0 = (grid_x * x) + offset_x;
		int x1 = (grid_x * (x + 1)) + offset_x;
		y0 = (y0 * grid_y) + offset_y;
		y1 = (y1 * grid_y) + offset_y;
		g.setColor(c);
		g.drawLine(x0, y0, x1, y1);
	}

	private void drawCenterString(Graphics g, String s, int x, int y) {
		x = x - (g.getFontMetrics().stringWidth(s) / 2);
		g.setColor(Color.black);
		g.drawString(s, x, y + 5);
	}

	private void drawRightString(Graphics g, String s, int x, int y) {
		x = x - g.getFontMetrics().stringWidth(s);
		g.setColor(Color.black);
		g.drawString(s, x, y + 5);
	}

	@Override
	public void paint(Graphics g) {
		if (this.hmm == null) {
			return;
		}
		DecimalFormat form = new DecimalFormat("0.0000");
		int nsymbols = this.hmm.symbols.size();
		int nstates = this.hmm.states.size();
		// complete graph.
		for (int i = 0; i < nsymbols; i++) {
			int offset_ymax = offset_y2 + (nstates * grid_y);
			if (i < (nsymbols - 1)) {
				for (int y1 = 0; y1 < nstates; y1++) {
					for (int y0 = 0; y0 < nstates; y0++) {
						Color c = link_normal;
						if ((this.hmm.stage == (i + 1)) && (this.hmm.i0 == y0) && (this.hmm.i1 == y1)) {
							c = link_processed;
						}
						if (this.hmm.matrix_prevstate[i + 1][y1] == y0) {
							c = link_maximum;
						}
						this.drawLink(g, i, y0, y1, c);
						if ((c == link_maximum) && (0 < i)) {
							double transprob = this.hmm.states.get(y0).transprob(this.hmm.states.get(y1));
							this.drawCenterString(g, form.format(transprob),
									offset_x + (i * grid_x) + (grid_x / 2), offset_ymax);
							offset_ymax = offset_ymax + 16;
						}
					}
				}
			}
			// state circles.
			for (int y = 0; y < nstates; y++) {
				Color c = state_fill;
				if (this.hmm.matrix_prevstate[i][y] != -1) {
					c = state_fill_maximum;
				}
				if ((this.hmm.sequence.size() == nsymbols) &&
						(this.hmm.sequence.get(nsymbols - 1 - i) == y)) {
					c = state_fill_best;
				}
				this.drawState(g, i, y, c);
			}
		}
		// max probability.
		for (int i = 0; i < nsymbols; i++) {
			for (int y1 = 0; y1 < nstates; y1++) {
				if (this.hmm.matrix_prevstate[i][y1] != -1) {
					this.drawCenterString(g, form.format(this.hmm.matrix_maxprob[i][y1]),
							offset_x + (i * grid_x), offset_y + (y1 * grid_y));
				}
			}
		}

		// captions (symbols atop)
		for (int i = 0; i < nsymbols; i++) {
			this.drawCenterString(g, this.hmm.symbols.get(i).name, offset_x + (i * grid_x), col_y);
		}
		// captions (states in left)
		for (int y = 0; y < nstates; y++) {
			this.drawRightString(g, this.hmm.states.get(y).name, col_x, offset_y + (y * grid_y));
		}

		// status bar
		g.setColor(Color.black);
		g.drawString(this.hmm.status, col_x, offset_y3 + (nstates * grid_y));
		g.drawString(this.hmm.status2, col_x, offset_y3 + (nstates * grid_y) + 16);
	}
}

class HMMDecoder {
	StateList states;
	int state_start;
	int state_end;

	public IntegerList sequence;
	public double[][] matrix_maxprob;
	public int[][] matrix_prevstate;
	public SymbolList symbols;
	public double probmax;
	public int stage, i0, i1;
	public boolean laststage;
	public String status, status2;

	public HMMDecoder() {
		this.status = "Not initialized.";
		this.status2 = "";
		this.states = new StateList();
	}

	public void addStartState(State st) {
		this.state_start = this.states.size(); // get current index
		this.states.add(st);
	}

	public void addNormalState(State st) {
		this.states.add(st);
	}

	public void addEndState(State st) {
		this.state_end = this.states.size(); // get current index
		this.states.add(st);
	}

	// for debugging.
	public void showmatrix() {
		for (int i = 0; i < this.symbols.size(); i++) {
			for (int j = 0; j < this.states.size(); j++) {
				System.out.print(this.matrix_maxprob[i][j] + " " + this.matrix_prevstate[i][j] + ", ");
			}
			System.out.println();
		}
	}

	// initialize for decoding
	public void initialize(SymbolList syms) {
		// symbols[syms.length] should be END
		this.symbols = syms;
		this.matrix_maxprob = new double[this.symbols.size()][this.states.size()];
		this.matrix_prevstate = new int[this.symbols.size()][this.states.size()];
		for (int i = 0; i < this.symbols.size(); i++) {
			for (int j = 0; j < this.states.size(); j++) {
				this.matrix_prevstate[i][j] = -1;
			}
		}

		State start = this.states.get(this.state_start);
		for (int i = 0; i < this.states.size(); i++) {
			this.matrix_maxprob[0][i] = start.transprob(this.states.get(i));
			this.matrix_prevstate[0][i] = 0;
		}

		this.stage = 0;
		this.i0 = -1;
		this.i1 = -1;
		this.sequence = new IntegerList();
		this.status = "Ok, let's get started...";
		this.status2 = "";
	}

	// forward procedure
	public boolean proceed_decoding() {
		this.status2 = "";
		// already end?
		if (this.symbols.size() <= this.stage) {
			return false;
		}
		// not started?
		if (this.stage == 0) {
			this.stage = 1;
			this.i0 = 0;
			this.i1 = 0;
			this.matrix_maxprob[this.stage][this.i1] = 0.0;
		} else {
			this.i0++;
			if (this.states.size() <= this.i0) {
				// i0 should be reinitialized.
				this.i0 = 0;
				this.i1++;
				if (this.states.size() <= this.i1) {
					// i1 should be reinitialized.
					// goto next stage.
					this.stage++;
					if (this.symbols.size() <= this.stage) {
						// done.
						this.status = "Decoding finished.";
						return false;
					}
					this.laststage = (this.stage == (this.symbols.size() - 1));
					this.i1 = 0;
				}
				this.matrix_maxprob[this.stage][this.i1] = 0.0;
			}
		}

		// sym1: next symbol
		Symbol sym1 = this.symbols.get(this.stage);
		State s0 = this.states.get(this.i0);
		State s1 = this.states.get(this.i1);

		// precond: 1 <= stage.
		double prob = this.matrix_maxprob[this.stage - 1][this.i0];
		DecimalFormat form = new DecimalFormat("0.0000");
		this.status = "Prob:" + form.format(prob);

		if (1 < this.stage) {
			// skip first stage.
			double transprob = s0.transprob(s1);
			prob = prob * transprob;
			this.status = this.status + " x " + form.format(transprob);
		}

		double emitprob = s1.emitprob(sym1);
		prob = prob * emitprob;
		this.status = this.status + " x " + form.format(emitprob) + "(" + s1.name + ":" + sym1.name + ")";

		this.status = this.status + " = " + form.format(prob);
		// System.out.println("stage: "+stage+", i0:"+i0+", i1:"+i1+", prob:"+prob);

		if (this.matrix_maxprob[this.stage][this.i1] < prob) {
			this.matrix_maxprob[this.stage][this.i1] = prob;
			this.matrix_prevstate[this.stage][this.i1] = this.i0;
			this.status2 = "(new maximum found)";
		}

		return true;
	}

	// backward proc
	public void backward() {
		int probmaxstate = this.state_end;
		this.sequence.add(probmaxstate);
		for (int i = this.symbols.size() - 1; 0 < i; i--) {
			probmaxstate = this.matrix_prevstate[i][probmaxstate];
			if (probmaxstate == -1) {
				this.status2 = "Decoding failed.";
				return;
			}
			this.sequence.add(probmaxstate);
			// System.out.println("stage: "+i+", state:"+probmaxstate);
		}
	}
}

public class Viterbi extends Applet implements ActionListener, Runnable {
	SymbolTable symtab;
	StateTable sttab;
	HMMDecoder myhmm = null;
	HMMCanvas canvas;
	Panel p;
	TextArea hmmdesc;
	TextField sentence;
	Button bstart, bskip;
	static final String initialHMM =
			"start: go(cow,1.0)\n" +
					"cow: emit(moo,0.9) emit(hello,0.1) go(cow,0.5) go(duck,0.3) go(end,0.2)\n" +
					"duck: emit(quack,0.6) emit(hello,0.4) go(duck,0.5) go(cow,0.3) go(end,0.2)\n";

	final int sleepmillisec = 100; // 0.1s

	// setup hmm
	// success:true.
	boolean setupHMM(String s) {
		this.myhmm = new HMMDecoder();
		this.symtab = new SymbolTable();
		this.sttab = new StateTable();

		State start = this.sttab.get("start");
		State end = this.sttab.get("end");
		this.myhmm.addStartState(start);

		boolean success = true;
		StringTokenizer lines = new StringTokenizer(s, "\n");
		while (lines.hasMoreTokens()) {
			// foreach line.
			String line = lines.nextToken();
			int i = line.indexOf(':');
			if (i == -1) {
				break;
			}
			State st0 = this.sttab.get(line.substring(0, i).trim());
			if ((st0 != start) && (st0 != end)) {
				this.myhmm.addNormalState(st0);
			}
			// System.out.println(st0.name+":"+line.substring(i+1));

			StringTokenizer tokenz = new StringTokenizer(line.substring(i + 1), ", ");
			while (tokenz.hasMoreTokens()) {
				// foreach token.
				String t = tokenz.nextToken().toLowerCase();
				if (t.startsWith("go(")) {
					State st1 = this.sttab.get(t.substring(3).trim());
					// fetch another token.
					if (!tokenz.hasMoreTokens()) {
						success = false; // err. nomoretoken
						break;
					}
					String n = tokenz.nextToken().replace(')', ' ');
					double prob;
					try {
						prob = Double.valueOf(n).doubleValue();
					} catch (NumberFormatException e) {
						success = false; // err.
						prob = 0.0;
					}
					st0.addLink(st1, prob);
					// System.out.println("go:"+st1.name+","+prob);
				} else if (t.startsWith("emit(")) {
					Symbol sym = this.symtab.intern(t.substring(5).trim());
					// fetch another token.
					if (!tokenz.hasMoreTokens()) {
						success = false; // err. nomoretoken
						break;
					}
					String n = tokenz.nextToken().replace(')', ' ');
					double prob;
					try {
						prob = Double.valueOf(n).doubleValue();
					} catch (NumberFormatException e) {
						success = false; // err.
						prob = 0.0;
					}
					st0.addSymbol(sym, prob);
					// System.out.println("emit:"+sym.name+","+prob);
				} else {
					// illegal syntax, just ignore
					break;
				}
			}

			st0.normalize(); // normalize probability
		}

		end.addSymbol(this.symtab.intern("end"), 1.0);
		this.myhmm.addEndState(end);

		return success;
	}

	// success:true.
	boolean setup() {
		if (!this.setupHMM(this.hmmdesc.getText())) {
			return false;
		}

		// initialize words
		SymbolList words = new SymbolList();
		StringTokenizer tokenz = new StringTokenizer(this.sentence.getText());
		words.add(this.symtab.intern("start"));
		while (tokenz.hasMoreTokens()) {
			words.add(this.symtab.intern(tokenz.nextToken()));
		}
		words.add(this.symtab.intern("end"));
		this.myhmm.initialize(words);
		this.canvas.setHMM(this.myhmm);
		return true;
	}

	@Override
	public void init() {
		this.canvas = new HMMCanvas();

		this.setLayout(new BorderLayout());
		this.p = new Panel();
		this.sentence = new TextField("moo hello quack", 20);
		this.bstart = new Button("  Start  ");
		this.bskip = new Button("Auto");
		this.bstart.addActionListener(this);
		this.bskip.addActionListener(this);
		this.p.add(this.sentence);
		this.p.add(this.bstart);
		this.p.add(this.bskip);
		this.hmmdesc = new TextArea(initialHMM, 4, 20);
		this.add("North", this.canvas);
		this.add("Center", this.p);
		this.add("South", this.hmmdesc);

	}

	void setup_fallback() {
		// adjustable
		State cow = this.sttab.get("cow");
		State duck = this.sttab.get("duck");
		State end = this.sttab.get("end");

		cow.addLink(cow, 0.5);
		cow.addLink(duck, 0.3);
		cow.addLink(end, 0.2);
		duck.addLink(cow, 0.3);
		duck.addLink(duck, 0.5);
		duck.addLink(end, 0.2);

		cow.addSymbol(this.symtab.intern("moo"), 0.9);
		cow.addSymbol(this.symtab.intern("hello"), 0.1);
		duck.addSymbol(this.symtab.intern("quack"), 0.6);
		duck.addSymbol(this.symtab.intern("hello"), 0.4);
	}

	@Override
	public void destroy() {
		this.remove(this.p);
		this.remove(this.canvas);
	}

	@Override
	public void processEvent(AWTEvent e) {
		if (e.getID() == Event.WINDOW_DESTROY) {
			System.exit(0);
		}
	}

	@Override
	public void run() {
		if (this.myhmm != null) {
			while (this.myhmm.proceed_decoding()) {
				this.canvas.repaint();
				try {
					Thread.sleep(this.sleepmillisec);
				} catch (InterruptedException e) {
					;
				}
			}
			this.myhmm.backward();
			this.canvas.repaint();
			this.bstart.setLabel("  Start  ");
			this.bstart.setEnabled(true);
			this.bskip.setEnabled(true);
			this.myhmm = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String label = ev.getActionCommand();

		if (label.equalsIgnoreCase("  start  ")) {
			if (!this.setup()) {
				// error
				return;
			}
			this.bstart.setLabel("Proceed");
			this.canvas.repaint();
		} else if (label.equalsIgnoreCase("proceed")) {
			// next
			if (!this.myhmm.proceed_decoding()) {
				this.myhmm.backward();
				this.bstart.setLabel("  Start  ");
				this.myhmm = null;
			}
			this.canvas.repaint();
		} else if (label.equalsIgnoreCase("auto")) {
			// skip
			if (this.myhmm == null) {
				if (!this.setup()) {
					// error
					return;
				}
			}
			this.bstart.setEnabled(false);
			this.bskip.setEnabled(false);
			Thread me = new Thread(this);
			me.setPriority(Thread.MIN_PRIORITY);
			// start animation.
			me.start();
		}
	}

	public static void main(String args[]) {
		Frame f = new Frame("Viterbi");
		Viterbi v = new Viterbi();
		f.add("Center", v);
		f.setSize(400, 400);
		f.show();
		v.init();
		v.start();
	}

	@Override
	public String getAppletInfo() {
		return "A Sample Viterbi Decoder Applet";
	}
}