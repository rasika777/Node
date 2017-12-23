import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Reply
{
	private String ip;
	private int timestamp;
	private String comment;

	public Reply(String ip, int timestamp, String comment)
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
	
	public String getReply()
	{
		return this.comment;
	}
	
	
	/* Sort messages by time stamp */
	public static Comparator<Reply> COMPARE_BY_TIMESTAMP = new Comparator<Reply>()
	{
		public int compare(Reply one, Reply other)
		{
			return Integer.compare(one.timestamp, other.timestamp);
		}
	};
}