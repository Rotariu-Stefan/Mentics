package com.mentics.qd.datastructures;

public class Response {
    public String qname;
    public String text;
    public String[] keywords;

    public Response(String qname, String text, String[] responses) {
    	this.qname = qname;
        this.text = text;
        this.keywords = responses;
    }
}
