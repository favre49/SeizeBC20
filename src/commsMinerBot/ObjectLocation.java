package commsMinerBot;
import battlecode.common.*;

public class ObjectLocation extends Globals
{
	//simple encapsulates ObjectType and MapLocation
	public ObjectType rt;
	public MapLocation loc;

	public ObjectLocation(ObjectType rt, MapLocation loc)
	{
		this.rt = rt;
		this.loc = loc;
	}

	public boolean equals(ObjectLocation other)
	{
		return (rt.equals(other.rt) && loc.equals(other.loc));
	}
}
