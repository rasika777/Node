import java.util.Comparator;

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
	
	/* Sort messages by time stamp */
	public static Comparator<Message> COMPARE_BY_TIMESTAMP = new Comparator<Message>()
	{
		public int compare(Message one, Message other)
		{
			return Integer.compare(one.timestamp, other.timestamp);
		}
	};
}