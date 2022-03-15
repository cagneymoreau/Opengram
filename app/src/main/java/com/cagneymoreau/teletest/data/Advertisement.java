package com.cagneymoreau.teletest.data;

import android.util.Log;

import com.google.gson.Gson;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

/**
 * Represents a user personal posts
 *
 * or Represents anther users posts
 *
 * In order for a search function to work these must be maintained in the heap.
 * Keep size under 1kb to prevent heap overflow if we maintain all in memory
 */



public class Advertisement {


    //region ----definitions

    private static String TAG = "Advertisement";

    public enum Source {SELF, OTHER}

    String div1 = ",";
    String div2 = "_";

    public static final String TELE_FLAG_V1 = "<!-- $#%telemarket%#$-->";

    public static final String ERROR_FLAG = "unable to build from html";

    private final long stale = 90L * 24L * 60L * 60L * 1000L;


    //endregion


    //region --------------------------------- tracking info

    /**
     * Telegrams assigned user id
     */
    long user;
    public long getUser()
    {
        return user;
    }

    public long getSellerId()
    {
        return user;
    }
    /**
     * original creation date. this will act to store date as well as a unique post id when combined with user id
     */
    long date;

    //newest versions timestampt
    long mostRecentUpdate;
    public void setMostRecentUpdate(long mostRecentUpdate) {
        this.mostRecentUpdate = mostRecentUpdate;
    }
    public long getMostRecentUpdate()
    {
        return mostRecentUpdate;
    }

    //the id that all parties can use to determine a unique advert
    String id;
    public String getId() {return id; }
    //public void setId(String id) { this.id = id; }

    /**
     * The chats this item should be posted to.
     * MUST BE PRIVATE or other users would see chats we belong to
     */
    private ArrayList<MessageLocation> proposedLocation = new ArrayList<>();
    public void setProposedLocation(ArrayList<MessageLocation> m) { proposedLocation = m; }
    public int getProposedLocationsSize()  {  return proposedLocation.size(); }
    public MessageLocation getProposedLocation(int p)  {  return proposedLocation.get(p);  }
    public boolean isProposedLocation(long id)
    {
        for (int i = 0; i < proposedLocation.size(); i++) {
            if (proposedLocation.get(i).getChatId() == id) return true;
        }
        return false;
    }

    /**
     * Where a posting is actually found at
     * when posting ads this is where our posts actually end up
     * when searching for ads this is where we found this add
     */
    private ArrayList<MessageLocation> actualLocations = new ArrayList<>();

    public void clearActuaLocations()
    {
        actualLocations.clear();
    }

    public void addActualLocation(TdApi.Message message)
    {
        if (!isMessageTheSame(message)) return;

        MessageLocation messageLocation = new MessageLocation(message.chatId, message.id);

        if (removeActualLocation(messageLocation)){
            new Exception().printStackTrace();
            Log.e(TAG, "addActualLocation: adding second location in same chat " + message.toString(), null);
        }
        actualLocations.add(messageLocation);
    }

    public boolean removeActualLocation(MessageLocation messageLocation)
    {
        int f = -1;
        for (int i = 0; i < actualLocations.size(); i++) {
            if (actualLocations.get(i).chat_id == messageLocation.chat_id) f = i;
        }

        if (f != -1){
            actualLocations.remove(f);
            return true;
        }
        return false;
    }


    private ArrayList<MessageLocation> foundLocations = new ArrayList<>();

    public void clearFoundLocations()
    {
        foundLocations.clear();
    }

    public void addFoundLocations(TdApi.Message message)
    {
        if (!isMessageTheSame(message)) return;

        MessageLocation messageLocation = new MessageLocation(message.chatId, message.id);

        foundLocations.add(messageLocation);
    }

    public boolean isLive(long id)
    {
        for (int i = 0; i < foundLocations.size(); i++) {
            if (foundLocations.get(i).getChatId() == id)return true;
        }
        return false;
    }

    //when user leaves a channel we remove all instance of those records. deletion of actual adds happens in marketcontroller
    public void leaveChannel(long id)
    {
        int found = -1;
        for (int i = 0; i < getProposedLocationsSize(); i++) {
            if (proposedLocation.get(i).getChatId() == id){
                found = i;
                break;
            }
        }
        if (found != -1) proposedLocation.remove(found);

        found = -1;
        for (int i = 0; i < getActualLocations().size(); i++) {
            if (actualLocations.get(i).getChatId() == id){
                found = i;
                break;
            }
        }
        if (found != -1) actualLocations.remove(found);

        found = -1;
        for (int i = 0; i < foundLocations.size(); i++) {
            if (foundLocations.get(i).getChatId() == id){
                found = i;
                break;
            }
        }
        if (found != -1) foundLocations.remove(found);


    }

    //endregion


    //region--------------------- content




    private String title = "";
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    private String description = "";
    public String getDescription() {  return description; }
    public void setDescription(String description) { this.description = description; }

    String amount = "";
    //date of actual event
    long expiration;

    public long getExpiration()
    {
        return expiration;
    }

    public void setExpiration(long expiration)
    {
        this.expiration = expiration;
    }

    Categories mycategory;

    public static int maxImages = 6;
    private ArrayList<Image> images;
    public int imagesSize() {  return images.size(); }
    public void addImage(Image i) { images.add(i); }
    public Image getImage(int pos)  {
        if (images.size() == 0)
        {
            return null;
        }
        return images.get(pos);
    }
    public boolean deleteImage(Image img)
    {
        int f = -1;
        for (int i = 0; i < images.size(); i++) {
            if (img.equals(images.get(i))){
                f = i;
            }
        }

        if (f != -1){
            images.remove(f);
            return true;
        }
        return false;
    }



    //endregion


    //region------------------ MyListings

    /**
     * Constructor for new ad
     * @param userId
     */
    public Advertisement(long userId)
    {
        user = userId;

        date = System.currentTimeMillis();
        mostRecentUpdate = date;

        id = user + "_" + date;

        images = new ArrayList<>();

    }


    /**
     * Generate a public friendly html file that can be posted on chats for other users to open
     * The html file that will be posted to the chat message
     * Done in two parts
     * 1) Is an html style file any app can read
     * 2) Is the GSON object to string posted as a comment so this app can quickly reinflate the object
     * @return
     */
    public String generateHTML()
    {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>").append("\n");
        html.append("<html lang=\"en\">").append("\n");
        html.append("    <head>\n" +
                "<style>\n" +
                "div.gallery {\n" +
                "  margin: 5px;\n" +
                "  border: 1px solid #ccc;\n" +
                "  float: left;\n" +
                "  width: auto;\n" +
                "}");
        html.append("div.gallery:hover {\n" +
                "  border: 1px solid #777;\n" +
                "}");
        html.append("div.gallery img {\n" +
                "  width: 100%;\n" +
                "  height: auto;\n" +
                "}");
        html.append("div.desc {\n" +
                "  padding: 15px;\n" +
                "  text-align: center;\n" +
                "}\n" +
                "</style>\n" +
                "</head>");
        html.append("<h1>").append(title).append("</h1>");
        html.append("<h2>").append(amount).append("</h2");

        html.append("<body><p>").append(description).append("</p>");

        for (int i = 0; i < images.size(); i++) {

            html.append(" <div class=\"gallery\">\n" +
                    "  <a target=\"_blank\" >")
                    .append(images.get(i).hostedFilehtml)
                    .append("</a>\n" +
                            "  \n" +
                            "</div>");
        }
        html.append("</body>");
        html.append("\n");

        // post the GSON for in app building

        html.append(TELE_FLAG_V1);
        html.append("\n");

        html.append("<!--");
        html.append("\n")
                .append(saveableAdvert(true));
        html.append("\n")
                .append("-->");

        html.append("</html>");

        return html.toString();

    }


    /**
     * Creates a dummy advert that could be used as a button in a recycleview a user would click to make a new advert
     * @return
     */
    public static Advertisement dummyAdvert() {

        Advertisement advertisement = new Advertisement(1234);
        advertisement.setTitle("Add new!");
        advertisement.id = "new";

        Image i = new Image();
        i.setBlank();
        advertisement.images.add(i);

        return advertisement;


    }




    //endregion


    //region----------------  listings I found on market

    /**
     * Build an advertisement from an html file
     * Ignore the html and reinflate with the commented GSON string
     * @return
     */
    public static Advertisement buildAdvert(ArrayList<String> htmlFile)
    {
        int count = -1;
        for (int i = 0; i < htmlFile.size(); i++) {

            if (htmlFile.get(i).equals(TELE_FLAG_V1)){
                count = i + 2;
            }
        }

        if (count == -1){
            return new Advertisement(ERROR_FLAG);
        }

        try {
         return restoredAdvert(htmlFile.get(count));
        }catch (Exception e)
        {
            Log.e(TAG, "buildAdvert: error building advert", null);
        }

        return new Advertisement(ERROR_FLAG);
    }


    //endregion



    //region------------------ shared

    /**
     * Used to quickly compare based on id
     * @param id
     */
    public Advertisement(String id)
    {
        this.id = id;

        if (id.equals(ERROR_FLAG)){

            title = ERROR_FLAG;
            description = ERROR_FLAG;


        }
    }


    /**
     * Build an advertisement from a string in shared preferences
     * @param savedPrefString
     * @return
     */
    public static Advertisement restoredAdvert(String savedPrefString)
    {
        Gson gson = new Gson();
        return  gson.fromJson(savedPrefString, Advertisement.class);
    }


    /**
     * Puts an advert into a String for saving in sharedpref
     * We must
     * @return
     */
    public String saveableAdvert(boolean publicView)
    {
        if (publicView)
        {


            ArrayList<MessageLocation> chosehold = proposedLocation;
            proposedLocation = new ArrayList<>();

            ArrayList<MessageLocation> act = actualLocations;
            actualLocations = new ArrayList<>();

            Gson gson = new Gson();
            String json = gson.toJson(this);

            actualLocations = act;
            proposedLocation = chosehold;

            return json;

        }

        Gson gson = new Gson();
        String json = gson.toJson(this);

        return json;
    }

    private boolean isMessageTheSame(TdApi.Message message)
    {
        if (message.content.getConstructor() != TdApi.MessageDocument.CONSTRUCTOR){

            message.toString();
            return false;
        }

        TdApi.MessageDocument messDoc = (TdApi.MessageDocument) message.content;
        String text = messDoc.caption.text;
        String[] parts = text.split("\n");
        if (!getTitle().equals(parts[parts.length-1])){
            Log.e(TAG, "addActualLocation: " + message.toString(), null);
            return false;
        }

        return true;
    }

    //endregion



    //region ---------------- helper classes


    public static ArrayList<Categories> getCategories()
    {

        ArrayList<Categories> c = new ArrayList<>();


        Categories wanted = new Categories();
        wanted.category = "wanted";
        wanted.description = "Items your searching for";
        c.add(wanted);

        Categories event = new Categories();
        event.category = "event";
        event.description = "A scheduled events information";
        c.add(event);

        Categories housing = new Categories();
        housing.category = "housing";
        housing.description = "For sale, rental, roomate";
        c.add(housing);

        Categories job = new Categories();
        job.category = "job";
        job.description = "Ful time work or odd jobs";
        c.add(job);

        Categories service = new Categories();
        service.category = "service";
        service.description = "Services being offered";
        c.add(service);

        Categories food = new Categories();
        food.category = "food/supplements";
        food.description = "Food supplements or similar";
        c.add(food);

        Categories farm = new Categories();
        farm.category = "farm/garden";
        farm.description = "Items your searching for";
        c.add(farm);

        Categories vehicle = new Categories();
        vehicle.category = "vehicle";
        vehicle.description = "Cars, motorcycles, boats, trailer aircraft, atv or their parts";
        c.add(vehicle);

        Categories outdoor = new Categories();
        outdoor.category = "outdoor/sport/tools";
        outdoor.description = "Outdoor equipment for camping, sports equipment or construction tools and material";
        c.add(outdoor);

        Categories household = new Categories();
        household.category = "household goods";
        household.description = "Appliances/toys/electronics/games/furniture";
        c.add(household);

        Categories financial = new Categories();
        financial.category = "financial";
        financial.description = "Investment or financial based goods. crypto/jewelry/precious metal,forex";
        c.add(financial);

        return c;

    }


    public static class Categories{

        String category;
        String description;

        public void setCategory(String s)
        {
            category = s;
        }

        public String getCategory()
        {
            return category;
        }

        public String toString()
        {
            return category;
        }

    }


    //endregion


    //region ------get/set

    /**
     * is the add over 90 days old or are some of the messages missing
     * @return
     */
    public boolean expired()
    {
       return  mostRecentUpdate < (System.currentTimeMillis() - stale);
    }

    public boolean missing()
    {
        return proposedLocation.size() != actualLocations.size();
    }

    public boolean deleted()
    {
        if(foundLocations.size() != actualLocations.size() || actualLocations.size() == 0){
            return true;
        }
        return false;
    }

    public boolean revoked() {return foundLocations.size() == 0;}



    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isAuthentic(TdApi.Message m)
    {
        if (m.sender.getConstructor() != TdApi.MessageSenderUser.CONSTRUCTOR) return false;

        TdApi.MessageSenderUser mUser = (TdApi.MessageSenderUser) m.sender;

        return mUser.userId == user;
    }



    public ArrayList<MessageLocation> getProposedLocation()
    {
        return proposedLocation;
    }


    public ArrayList<MessageLocation> getActualLocations()
    {
        return actualLocations;
    }


    public Categories getMycategory()
    {
        if (mycategory == null){
            mycategory = new Categories();
            mycategory.setCategory("Everything");
        }

        return mycategory;
    }

    public void setMycategory(Categories cat)
    {
        mycategory = cat;
    }

    public boolean postedHere(long id){

        if (id == 0) return true;

        for (int i = 0; i < actualLocations.size(); i++) {
            if (actualLocations.get(i).chat_id == id) return true;
        }
        return false;
    }



    //endregion




}
