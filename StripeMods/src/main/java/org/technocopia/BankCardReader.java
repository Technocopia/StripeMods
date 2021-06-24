package org.technocopia;
import jnasmartcardio.*;

import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class BankCardReader {
	public BankCardReader() throws CardException{
//		TerminalFactory terminalFactory = TerminalFactory.getDefault();
//		CardTerminals terminals = terminalFactory.terminals();
//		List<CardTerminal> terminalList = terminals.list();
//		
//		CardTerminal terminal = terminalList.get(0);
//		Card connection = terminal.connect("*");
//		ATR atr = connection.getATR();
//		byte[] atrBytes = atr.getBytes();
//		boolean hasNonZeroAtr = false;
//		for (int i = 0; i < atrBytes.length; i++) {
//			hasNonZeroAtr = hasNonZeroAtr || atrBytes[i] != 0;
//		}
		
	}
	
}
