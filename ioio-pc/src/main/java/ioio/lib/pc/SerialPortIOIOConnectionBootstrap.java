package ioio.lib.pc;

import ioio.lib.api.IOIOConnection;
import ioio.lib.spi.IOIOConnectionBootstrap;
import ioio.lib.spi.IOIOConnectionFactory;
import ioio.lib.spi.Log;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import purejavacomm.CommPort;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;

public class SerialPortIOIOConnectionBootstrap implements
		IOIOConnectionBootstrap {
	private static final String TAG = "SerialPortIOIOConnectionBootstrap";

	@Override
	public void getFactories(Collection<IOIOConnectionFactory> result) {
		Collection<String> ports = getExplicitPorts();
		if (ports == null) {
			Log.w(TAG, "ioio.SerialPorts not defined.\n"
					+ "Will attempt to enumerate all possible ports (slow) "
					+ "and connect to a IOIO over each one.\n"
					+ "To fix, add the -Dioio.SerialPorts=xyz argument to "
					+ "the java command line, where xyz is a colon-separated "
					+ "list of port identifiers, e.g. COM1:COM2.");
			ports = getAllOpenablePorts();
		}
		for (final String port : ports) {
			Log.d(TAG, "Adding serial port " + port);
			result.add(new IOIOConnectionFactory() {
				@Override
				public String getType() {
					return SerialPortIOIOConnection.class.getCanonicalName();
				}

				@Override
				public Object getExtra() {
					return port;
				}

				@Override
				public IOIOConnection createConnection() {
					return new SerialPortIOIOConnection(port);
				}
			});
		}
	}

	static Collection<String> getAllOpenablePorts() {
		List<String> result = new LinkedList<String>();
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier
				.getPortIdentifiers();
		while (identifiers.hasMoreElements()) {
			final CommPortIdentifier identifier = identifiers.nextElement();
			if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (checkIdentifier(identifier)) {
					Log.d(TAG, "Adding serial port " + identifier.getName());
					result.add(identifier.getName());
				} else {
					Log.w(TAG, "Serial port " + identifier.getName()
							+ " cannot be opened. Not adding.");
				}
			}
		}
		return result;
	}

	static Collection<String> getExplicitPorts() {
		String property = System.getProperty("ioio.SerialPorts");
		if (property == null) {
			return null;
		}
		List<String> result = new LinkedList<String>();
		String[] portNames = property.split(":");
		for (String portName : portNames) {
			result.add(portName);
		}
		return result;
	}

	static boolean checkIdentifier(CommPortIdentifier id) {
		if (id.isCurrentlyOwned()) {
			return false;
		}
		// The only way to find out is apparently to try to open the port...
		try {
			CommPort port = id.open(
					SerialPortIOIOConnectionBootstrap.class.getName(), 1000);
			port.close();
		} catch (PortInUseException e) {
			return false;
		}
		return true;
	}
}
