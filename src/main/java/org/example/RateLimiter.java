package org.example;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RateLimiter {
    private  int cap;
    private  int refill;
    private int intervalInMin;
    private  final String fixedUrl="YOUR_URL";
    private final String token="YOUR_TOKEN";
    private int ttl;
    public int getInterval()
    {
        return intervalInMin;
    }
    public RateLimiter(int cap,int refill,int interval,int ttl)
    {
        this.cap=cap;
        this.refill=refill;
        this.intervalInMin=interval;
        this.ttl=ttl;
    }
     public List<Integer> isAllowed(String userId) throws IOException, InterruptedException {
        int start,end,tokens,incr;
       long issuedat,currTime=System.currentTimeMillis();
        boolean f=true;
        List<Integer> lp=new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        String redisKey="rate_limit:"+userId;

        String URL=fixedUrl+"hget/"+redisKey+"/tokens";


        HttpRequest req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
        String resToken=client.send(req,BodyHandlers.ofString()).body();


        URL=fixedUrl+"hget/"+redisKey+"/issuedat";
        req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
        String resTime=client.send(req,BodyHandlers.ofString()).body();

        if(resToken.contains("\"error\"")|| resTime.contains("\"error\""))
        {
               lp.add(1);
               lp.add(cap);
               lp.add(-1);
        }
        else
        {
             start = resToken.indexOf(":") + 1;
             end = resToken.lastIndexOf("}");
            String value = resToken.substring(start, end).trim();
            value = value.replace("\"", "");
            tokens=value.equals("null")?cap:Integer.parseInt(value);


            start = resTime.indexOf(":") + 1;
            end = resTime.lastIndexOf("}");
            value=resTime.substring(start,end).trim();
            value = value.replace("\"", "");
            issuedat=value.equals("null")?currTime:Long.parseLong(value);

            if(issuedat==currTime)
            {
                tokens-=1;

                URL=fixedUrl+"hset/"+redisKey+"/tokens/"+tokens+"/issuedat/"+issuedat;
                req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
                client.send(req,BodyHandlers.ofString());
                URL=fixedUrl+"expire/"+redisKey+"/"+ttl;
                req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
                client.send(req,BodyHandlers.ofString());
                lp.add(1);
                lp.add(tokens);
                lp.add(-1);
            }
            else
            {
                long t=(1000*60*intervalInMin);
                incr=(int)((currTime-issuedat)/t)*refill;

                if(tokens>=1)
                {
                    tokens-=1;
                    if(incr==0)
                    {

                        URL=fixedUrl+"hset/"+redisKey+"/tokens/"+tokens;
                        req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
                        client.send(req,BodyHandlers.ofString());
                        lp.add(1);
                        lp.add(tokens);
                        lp.add(-1);
                    }
                    else
                    {

                        tokens=Math.min(cap,tokens+incr);
                        URL=fixedUrl+"hset/"+redisKey+"/tokens/"+tokens+"/issuedat/"+currTime;
                        req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
                        client.send(req,BodyHandlers.ofString());
                        lp.add(1);
                        lp.add(tokens);
                        lp.add(-1);

                    }
                }
                else
                {
                    if(incr==0)
                    {
                            lp.add(0);
                            lp.add(tokens);
                            double d=(double)(currTime-issuedat)/1000D;
                            d=Math.ceil(d);
                            lp.add(intervalInMin*60-(int)d);
                    }
                    else
                    {

                        tokens=Math.min(cap,incr);
                        tokens-=1;
                        URL=fixedUrl+"hset/"+redisKey+"/tokens/"+tokens+"/issuedat/"+currTime;
                        req=HttpRequest.newBuilder().uri(URI.create(URL)).header("Authorization","Bearer "+token).build();
                        client.send(req,BodyHandlers.ofString());
                        lp.add(1);
                        lp.add(tokens);
                        lp.add(-1);
                    }
                }
            }

        }
        return lp;
    }

    static void main() {
        long a=268,b=1234;
        double d=(b-a)/1000D;
        d=Math.ceil(d);
        System.out.println(d);
    }

}
