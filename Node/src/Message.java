import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Message
{
	private String ip;
	private int timestamp;
	private String comment;
	private ArrayList<Reply> replies;

	public Message(String ip, int timestamp, String comment, ArrayList<Reply> reply_list)
	{
		this.ip = ip;
		this.timestamp = timestamp;
		this.comment = comment;
		//replies = reply_list;
		this.replies = new ArrayList<Reply>();
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
	
	public List<Reply> getReplies()
	{
		return this.replies;
	}
	
	public void setReplies(Reply reply)
	{
		this.replies.add(reply);
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