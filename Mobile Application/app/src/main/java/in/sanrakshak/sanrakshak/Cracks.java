package in.sanrakshak.sanrakshak;

public class Cracks {
    private String latitude,longitude,name,city,intensity,date,preview;
    Cracks(String latitude,String longitude,String name,String city,String intensity, String date, String preview) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.city = city;
        this.intensity = intensity;
        this.date = date;
        this.preview = preview;
    }
    public String getLatitude() {return latitude;}
    public void setLatitude(String latitude) {this.latitude = latitude;}
    public String getLongitude() {return longitude;}
    public void setLongitude(String longitude) {this.longitude = longitude;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getCity() {return city;}
    public void setCity(String city) {this.city = city;}
    public String getIntensity() {return intensity;}
    public void setIntensity(String intensity) {this.intensity = intensity;}
    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}
    public String getPreview() {return preview;}
    public void setPreview(String preview) {this.preview = preview;}
}