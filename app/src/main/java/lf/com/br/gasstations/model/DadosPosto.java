package lf.com.br.gasstations.model;

/**
 * Created by Fernando on 23/05/2016.
 */
public class DadosPosto {

    private Integer id;
    private String nome;
    private String comments;
    private String lat;
    private String lon;


    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String coments) {
        this.comments = coments;
    }

    @Override
    public String toString() {
        return comments;
    }
}
