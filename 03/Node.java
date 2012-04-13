class Node {
	private final String address;
	private int age;

	public Node(String address) {
		this.address = address;
		this.age = 0;
	}

	public Node(String address, int age) {
		this.address = address;
		this.age = age;
	}

	public String getAddress() {
		return this.address;
	}

	public int getAge() {
		return this.age;
	}

	public void age() {
		this.age++;
	}
	
	public boolean compareAddress(Node node) {
		if (this.address.equals(node.getAddress())) {
			return true;
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
