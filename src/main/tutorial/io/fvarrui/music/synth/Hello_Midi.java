package io.fvarrui.music.synth;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.ShortMessage;

import io.fvarrui.music.midi.MidiKeyboard;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class Hello_Midi {

	WavePlayer sine, square;
	Gain sineGain;
	Glide gainGlide;

	public static void main(String[] args) {
		Hello_Midi synth = new Hello_Midi();
		synth.setup();
	}

	// construct the synthesizer
	public void setup() {
		AudioContext ac = new AudioContext();

		sine = new WavePlayer(ac, 440.0f, Buffer.SINE);
		square = new WavePlayer(ac, 440.0f, Buffer.SQUARE);

		// set up the Gain and Glide objects and connect them
		gainGlide = new Glide(ac, 0.0f, 50.0f);
		sineGain = new Gain(ac, 1, gainGlide);
		sineGain.addInput(sine);
		sineGain.addInput(square);
		ac.out.addInput(sineGain);

		ac.start();

		// set up the keyboard input
		MidiKeyboard keys = new MidiKeyboard();
		keys.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if the event is not null
				if (e != null) {
					// if the event is a MIDI event
					if (e.getSource() instanceof ShortMessage) {
						// get the MIDI event
						ShortMessage sm = (ShortMessage) e.getSource();

						System.out.println("message ----------------------");
						System.out.println("- command : " + sm.getCommand());
						System.out.println("- channel : " + sm.getChannel());
						System.out.println("- data1   : " + sm.getData1());
						System.out.println("- data2   : " + sm.getData2());
						System.out.println("- length  : " + sm.getLength());
						System.out.println("- status  : " + sm.getStatus());
						
						// if the event is a key down
						if (sm.getCommand() == MidiKeyboard.NOTE_ON && sm.getData2() > 1) {
							System.out.println("keydown");
							keyDown(sm.getData1());
						}
						// if the event is a key up
						else {
							System.out.println("keyup");
							keyUp(sm.getData1());
						}
					}
				}
			}
		});
	}

	private float pitchToFrequency(int midiPitch) {
		/*
		 * MIDI pitch number to frequency conversion equation from
		 * http://newt.phys.unsw.edu.au/jw/notes.html
		 */
		double exponent = (midiPitch - 69.0) / 12.0;
		return (float) (Math.pow(2, exponent) * 440.0f);
	}

	public void keyDown(int midiPitch) {
		if (sine != null && gainGlide != null) {
			sine.setFrequency(pitchToFrequency(midiPitch));
			square.setFrequency(pitchToFrequency(midiPitch));
			gainGlide.setValue(0.9f);
		}
	}

	public void keyUp(int midiPitch) {
		if (gainGlide != null) {
			gainGlide.setValue(0.0f);
		}
	}

}
