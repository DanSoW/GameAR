package com.game.app

import android.content.Context
import android.os.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    data class Data(
        @SerializedName("number") var number: Int?,
        @SerializedName("name") var name: String,
        @SerializedName("author") var author: String
    )

    data class ErrorData(
        @SerializedName("message") var message: String
    );

    private var _txtNumberBook: EditText? = null;
    private var _txtNameBook: EditText? = null;
    private var _txtNameAuthor: EditText? = null;
    private var _txtNumberBookGet: EditText? = null;

    private var _btnAddData: Button? = null;
    private var _btnGetData: Button? = null;

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _txtNumberBook = findViewById<EditText>(R.id.txtNumberBook);
        _txtNameBook = findViewById<EditText>(R.id.txtNameBook);
        _txtNameAuthor = findViewById<EditText>(R.id.txtNameAuthor);
        _txtNumberBookGet = findViewById<EditText>(R.id.txtNameBookGet);
        _btnAddData = findViewById<Button>(R.id.btnAddData);
        _btnGetData = findViewById<Button>(R.id.btnGetData);

        _btnAddData?.setOnClickListener {
            try{
                var value = _txtNumberBook?.text.toString().toInt();
                if(value < 0){
                    throw Exception();
                }

            }catch(e: Exception){
                Toast.makeText(applicationContext, "Error: the book number is a sequence of digits!", Toast.LENGTH_LONG).show();
                return@setOnClickListener;
            }

            if((_txtNameBook?.text?.isEmpty() == true)
                || (_txtNameAuthor?.text?.isEmpty() == true)){
                Toast.makeText(applicationContext, "Error: you need to fill in the input fields!", Toast.LENGTH_LONG).show();
                return@setOnClickListener;
            }

            TransportDataPOST(applicationContext).execute(_txtNumberBook?.text.toString(),
                _txtNameBook?.text.toString(), _txtNameAuthor?.text.toString());
        };

        _btnGetData?.setOnClickListener{
            try{
                var value = _txtNumberBookGet?.text.toString().toInt();
                if(value < 0){
                    throw Exception();
                }

            }catch(e: Exception){
                Toast.makeText(applicationContext, "Error: the book number is a sequence of digits!", Toast.LENGTH_LONG).show();
                return@setOnClickListener;
            }

            TransportDataGET(applicationContext, _txtNumberBook!!, _txtNameBook!!, _txtNameAuthor!!).execute(_txtNumberBookGet?.text.toString().toInt());
        };
    }

    class TransportDataGET(con: Context, text1: EditText, text2: EditText, text3: EditText) : AsyncTask<Int, Int, Data>(){
        private var context = con;
        private var outputData: Array<Data>? = null;

        private var _text1 = text1;
        private var _text2 = text2;
        private var _text3 = text3;

        override fun onPostExecute(result: Data?) {
            super.onPostExecute(result);
            if (result != null) {
                _text1.setText(result.number.toString());
                _text2.setText(result.name);
                _text3.setText(result.author);
            }
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg params: Int?): Data? {
            var url: URL? = null;

            try{
                url = URL("http://10.0.2.2:8080/book/get/" + params[0]);
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: unable to establish a connection to the server!", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            var http: HttpURLConnection? = null;

            try{
                http = url.openConnection() as HttpURLConnection;
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: unable to establish a connection to the server!", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            http.requestMethod = "GET";

            var input = BufferedReader(InputStreamReader(http.inputStream));
            var inputLine: String? = null;
            var response = StringBuffer();

            inputLine = input.readLine();

            while(inputLine != null){
                response.append(inputLine);
                inputLine = input.readLine();
            }

            input.close();
            http.disconnect();

            var gson = Gson();

            try{
                var outputData = gson.fromJson(response.toString(), Array<Data>::class.java);
                println(outputData[0].name);
                return outputData[0];
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: data cannot be read from the database!", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            return null;
        }
    }

    class TransportDataPOST(con: Context) : AsyncTask<String, String, String>(){
        private var context = con;

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg params: String?): String {
            var url: URL? = null;

            try{
                url = URL("http://10.0.2.2:8080/book");
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: unable to establish a connection to the server!", Toast.LENGTH_LONG).show();
                }
                return "";
            }

            var http: HttpURLConnection? = null;

            try{
                http = url.openConnection() as HttpURLConnection;
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: unable to establish a connection to the server!", Toast.LENGTH_LONG).show();
                }
                return "";
            }

            http.requestMethod = "POST";
            http.doOutput = true;
            http.setRequestProperty("Content-Type", "application/json");

            var data: Data? = Data(params[0].toString().toInt(), params[1].toString(), params[2].toString());

            var gson = Gson();
            var strData = gson.toJson(data);
            var out = strData.toByteArray(StandardCharsets.UTF_8);

            var stream: OutputStream? = null;

            try{
                stream = http?.outputStream;
            }catch(e: Exception){
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: Unable to open the stream for writing!", Toast.LENGTH_LONG).show();
                }
            }

            stream?.write(out);

            var res: String;

            var input = BufferedReader(InputStreamReader(http.inputStream));
            var inputLine: String? = null;
            var response = StringBuffer();

            inputLine = input.readLine();

            while(inputLine != null){
                response.append(inputLine);
                inputLine = input.readLine();
            }

            input.close();
            res = response.toString();

            http.disconnect();

            if (res != null && res.length !== 0 && res.contains("message")) {
                Handler(Looper.getMainLooper()).post {
                    val mes: ErrorData = gson.fromJson(res, ErrorData::class.java)
                    Toast.makeText(context, mes.message, Toast.LENGTH_LONG).show();
                }
            }

            return "";
        }
    }
}