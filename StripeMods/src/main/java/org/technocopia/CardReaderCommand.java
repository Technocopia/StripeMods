package org.technocopia;

import edu.wpi.SimplePacketComs.FloatPacketType;
import edu.wpi.SimplePacketComs.phy.HIDSimplePacketComs;

public class CardReaderCommand extends HIDSimplePacketComs {
	FloatPacketType readCard = new FloatPacketType(44, 64);
	double[] card=new double[1];
	private IOnCardRead gotCard =null;
	public CardReaderCommand(){
		super(0x16c0,  0x486);
		connect();
		readCard.pollingMode();
		addPollingPacket(readCard);
		
		addEvent(readCard.idOfCommand, () -> {
			readFloats(readCard.idOfCommand, card);
			long value = Math.round(card[0]);
			if(value>0 && gotCard!=null) {
				gotCard.event(value);
			}
		});
	}

	public void setGotCard(IOnCardRead gotCard) {
		this.gotCard = gotCard;
	}
}
