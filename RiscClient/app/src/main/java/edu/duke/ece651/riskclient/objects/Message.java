package edu.duke.ece651.riskclient.objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

@Entity
public class Message implements IMessage {
    /**
     * Make all this field public, so that Room can access them directly.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "roomID")
    public int roomID;
    @ColumnInfo(name = "author")
    public IUser user;
    @ColumnInfo(name = "content")
    public String msg;
    @ColumnInfo(name = "date")
    public Date date;

    public Message(int id, int roomID, IUser user, String msg) {
        this.id = id;
        this.roomID = roomID;
        this.user = user;
        this.msg = msg;
        this.date = new Date();
    }

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public String getText() {
        return msg;
    }

    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }
}