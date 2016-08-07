package lf.com.br.gasstations;



import lf.com.br.gasstations.model.PostoCatalog;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Fernando on 20/04/2016.
 */
public interface PostoService {

    String BASE_URL = "http://api.meuspostos.com.br/";

   // @GET("busca.json?lat=-22.7675157&lon=-43.4258355")
    @GET("busca.json")
    Call<PostoCatalog> listaPostos(@Query("lat") String latitude, @Query("lon") String longitude);

    @GET("busca.json")
    Call<PostoCatalog> listaPostosPorNome(@Query("chave") String nomePosto);


}
