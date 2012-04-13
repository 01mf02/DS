class Node {
	private final String address;
	private final int port;
	private int age;

	public Node(String address, int port) {
		this.address = address;
		this.port = port;
		this.age = 0;
	}

	public Node(String address, int port, int age) {
		this.address = address;
		this.port = port;
		this.age = age;
	}

	public String getAddress() {
		return this.address;
	}

	public int getPort() {
		return this.port;
	}

	public int getAge() {
		return this.age;
	}

	public void age() {
		this.age++;
	}

	public boolean compareAddressAndPort(Node node) {
		if (this.address.equals(node.getAddress())) {
			if (this.port == node.getPort()) {
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	public boolean compareAge(Node node) {
		if (this.age > node.age) {
			return true;
		} else {
			return false;
		}
	}

}
