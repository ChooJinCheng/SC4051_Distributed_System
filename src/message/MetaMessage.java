package message;

import models.Metadata;

public class MetaMessage extends BaseMessage{
    private Metadata metadata;

    public MetaMessage(){
        super();
    }


    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
