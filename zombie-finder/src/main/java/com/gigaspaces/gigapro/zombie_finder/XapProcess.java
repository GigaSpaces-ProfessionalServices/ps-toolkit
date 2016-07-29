package com.gigaspaces.gigapro.zombie_finder;

public class XapProcess {
	private String host;
	private String processId;
	private String serviceType;
//	private String command;

    public XapProcess(String host, String processId, String serviceType) {
		this.host = host;
		this.processId = processId;
		this.serviceType = serviceType;
	}

	public String getHost() {
		return host;
	}

	public String getProcessId() {
		return processId;
	}

	public String getServiceType() {
		return serviceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((processId == null) ? 0 : processId.hashCode());
		result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XapProcess other = (XapProcess) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		if (serviceType == null) {
			if (other.serviceType != null)
				return false;
		} else if (!serviceType.equals(other.serviceType))
			return false;
		return true;
	}

	/*public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}*/
}
