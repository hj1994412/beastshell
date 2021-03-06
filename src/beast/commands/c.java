package beast.commands;

import java.util.ArrayList;
import java.util.List;

import bsh.Interpreter;
import bsh.CallStack;

public class c {

	public static String usage() {
		return "usage: c( object1, object2, ..., objectk)\n       x = c(1.0, 2.0, 3.0)";
	}

	/**
		Implement c() command.
	*/
	public static List invoke( Interpreter env, CallStack callstack ) 
	{
		return new ArrayList();
	}

	/**
		Implement c(Object ... values) command.
	*/
	public static List invoke( 
		Interpreter env, CallStack callstack, Object ... objects ) 
	{
		List list = new ArrayList();
		for (Object o : objects) {
			list.add(o);
		}
		return list;
	}
}
