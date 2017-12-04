class Message
{
	private String ip;
	private int timestamp;
	private String comment;

	public Message(String ip, int timestamp, String comment)
	{
		this.ip = ip;
		this.timestamp = timestamp;
		this.comment = comment;
	}	

	public String getIp()
	{
		return this.ip;
	}

	public int getTimestamp()
	{
		return this.timestamp;
	}
	
	public String getComment()
	{
		return this.comment;
	}
}