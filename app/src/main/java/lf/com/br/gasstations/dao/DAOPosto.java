package lf.com.br.gasstations.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lf.com.br.gasstations.model.DadosPosto;

/**
 * Created by Fernando on 23/05/2016.
 */
public class DAOPosto extends SQLiteOpenHelper {


    private static final int VERSAO = 1;
    private static final String NAME = "BDPosto";
    private static final String TABELA = "Posto";

    public DAOPosto(Context context) {
        super(context, NAME, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String ddl = "CREATE TABLE "+ TABELA + "(id INTEGER, nome TEXT, comments TEXT, lat TEXT, lon TEXT);";
        db.execSQL(ddl);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String ddl = "DROP TABLE IF EXISTS "+ TABELA;
        db.execSQL(ddl);
        onCreate(db);
    }

    public void inserePosto(DadosPosto posto){
        ContentValues values = new ContentValues();
        values.put("id", posto.getId());
        values.put("nome", posto.getNome());
        values.put("comments", posto.getComments());
        values.put("lat", posto.getLat());
        values.put("lon", posto.getLon());
        getWritableDatabase().insert(TABELA,null, values);
    }

    public List<DadosPosto> getListaPostos(){
        List<DadosPosto> listaPostos = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery("SELEC * FROM " + TABELA + ";", null);

        while (cursor.moveToNext()){
            DadosPosto posto = new DadosPosto();

            posto.setId(cursor.getInt(cursor.getColumnIndex("id")));
            posto.setNome(cursor.getString(cursor.getColumnIndex("nome")));
            posto.setComments(cursor.getString(cursor.getColumnIndex("comments")));
            posto.setLat(cursor.getString(cursor.getColumnIndex("lat")));
            posto.setLon(cursor.getString(cursor.getColumnIndex("lon")));

            listaPostos.add(posto);
        }

        return listaPostos;
    }

    public DadosPosto getPosto(Integer idPosto){
        DadosPosto posto = new DadosPosto();
        String[] args = {idPosto.toString()};
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABELA + " WHERE id=?;", args);

        cursor.moveToFirst();

        posto.setId(cursor.getInt(cursor.getColumnIndex("id")));
        posto.setNome(cursor.getString(cursor.getColumnIndex("nome")));
        posto.setComments(cursor.getString(cursor.getColumnIndex("comments")));
        posto.setLat(cursor.getString(cursor.getColumnIndex("lat")));
        posto.setLon(cursor.getString(cursor.getColumnIndex("lon")));

        cursor.close();
        return posto;
    }

    public void addComments(DadosPosto dadosPosto){

        ContentValues values = new ContentValues();
        values.put("comments", dadosPosto.getComments());
        getWritableDatabase().update(TABELA, values, "id+?",
                new String[]{dadosPosto.getId().toString()});
    }
}
