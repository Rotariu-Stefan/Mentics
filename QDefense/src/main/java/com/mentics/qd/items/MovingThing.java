package com.mentics.qd.items;


public class MovingThing {

    public MovingThing() {}

    public float position[] = new float[3];
    public float velocity[] = new float[3];

    // If a command adds some force that is initiated by the MovingThing itself, it should use initiatedForce
    public float initiatedForce[] = new float[3];// replace with rigidbody force

    // If a command adds an external force it should modify force directly
    public float force[] = new float[3];// replace with rigidbody force
    public float mass = 1f;
    
    public MovingThing(MovingThing other) {
    	this.position = other.position;
    	this.velocity = other.velocity;
    	this.initiatedForce = other.initiatedForce;
    	this.force = other.force;
    	this.mass = other.mass;
    }

}
