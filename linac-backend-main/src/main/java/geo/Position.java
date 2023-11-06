package geo;


public class Position {
	
	private int x;
	private int y;
	
	/**
	 * @param x
	 * @param y
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position() {
	}
	
	public double distance(Position position) {
		return distanceEuclidian(position);
	}
	
	public double distanceEuclidian(Position position) {
		return Math.hypot(position.getX() - this.x, position.getY() - this.y);
	}
	
	public double distanceManhattan(Position position) {
		return Math.abs(this.x - position.getX()) + Math.abs(this.y - position.getY());
	}
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void translate(int x, int y) {
		this.x = this.x + x;
		this.y = this.y + y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + "]";
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}

	
	
}
