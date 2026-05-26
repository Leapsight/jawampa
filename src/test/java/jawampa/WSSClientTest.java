package jawampa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import jawampa.auth.client.Cryptosign;
import jawampa.auth.client.Password;
import jawampa.connection.IWampConnectorProvider;
import jawampa.transport.netty.NettyWampClientConnectorProvider;
import jawampa.transport.netty.NettyWampConnectionConfig;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import javax.net.ssl.SNIHostName;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WSSClientTest {

   public static void main(String[] args) {
      new WSSClientTest().start();
   }

   Subscription addProcSubscription;
   Subscription eventPublication;
   Subscription eventSubscription;

   static final int eventInterval = 2000;
   int lastEventValue = 0;

   public void start() {

      IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
      WampClientBuilder builder = new WampClientBuilder();

      // Build two clients
      final WampClient client1;
      try {

         // acc: 8d9b6050-b254-44f5-9080-ca7b61b1c4d5
         // urn:wellos:id:${APP_WELLOS_ACCOUNT_ID}:data_reader_adapter
         //4ffddd896a530ce5ee8c86b83b0d31835490a97a9cd718cb2f09c9fd31c4a7d7
         //1766c9e6ec7d7b354fd7a2e4542753a23cae0b901228305621e5b8713299ccdd
         LoggingHandler loggingHandler = new LoggingHandler();

         SslContext sslContext = SslContextBuilder.forClient()
                 .trustManager(io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE)
                 //.protocols("TLSv1.3")
                 .serverName(new SNIHostName("localhost"))
                 .build();

         builder.withConnectorProvider(connectorProvider)
                 .withUri("wss://localhost:18080/ws")
                 .withRealm("com.pae.upstream")
                 //.withAuthId("urn:wellos:id:8d9b6050-b254-44f5-9080-ca7b61b1c4d5:data_reader_adapter")
                 .withAuthId("user_service")
                 .withAuthMethod(new Cryptosign(
                         "4ffddd896a530ce5ee8c86b83b0d31835490a97a9cd718cb2f09c9fd31c4a7d71766c9e6ec7d7b354fd7a2e4542753a23cae0b901228305621e5b8713299ccdd",
                         "1766c9e6ec7d7b354fd7a2e4542753a23cae0b901228305621e5b8713299ccdd"))
                 //.withInfiniteReconnects()
                 .withReconnectInterval(5, TimeUnit.SECONDS)
                 .withConnectionConfiguration((new NettyWampConnectionConfig.Builder()
                         .withSslContext(sslContext)
                           ).build());

         client1 = builder.build();
         client1.open();
      } catch (Exception e) {
         e.printStackTrace();
         return;
      }

      client1.statusChanged().subscribe(new Action1<WampClient.State>() {
         @Override
         public void call(WampClient.State t1) {
            System.out.println("Session1 status changed to " + t1);

            if (t1 instanceof WampClient.ConnectedState) {
               // Register a procedure

               ObjectMapper mapper = new ObjectMapper();
               ArrayNode id = mapper.createArrayNode();

               String kwarg = "{\"security\":" +
                       "         {          " +
                       "         \"authid\":\"bismark.vasquez@leapsight.com\"," +
                       "         \"client_id\":\"ycw3aqqazrn9flgrebdxquhewr8b6v2v\"," +
                       "         \"groups\": [\"business_account_admin\",\"resource_owners\"]," +
                       "         \"locale\": \"es_AR\"," +
                       "         \"meta\":" +
                       "            {" +
                       "            \"account_id\":\"mrn:account:business:5bb69d77-3fa9-4cbb-829b-09457a12ac3e\"," +
                       "            \"business_account_id\":\"mrn:account:business:5bb69d77-3fa9-4cbb-829b-09457a12ac3e\"," +
                       "            \"business_user_id\":\"mrn:person:5f201f21-c29e-4eb7-aaae-68fa159f29dc\"," +
                       "            \"user_id\":\"mrn:person:5f201f21-c29e-4eb7-aaae-68fa159f29dc\"" +
                       "            }," +
                       "         \"realm_uri\": \"com.magenta.test\", " +
                       "         \"session\": 7466872937963856, " +
                       "         \"username\":\"bismark.vasquez@leapsight.com\"}" +
                       "      }";

               id.add("mrn:agent:9099ac5e-12fd-4c46-b3a0-801099c16759");
               id.add(kwarg);

               Observable<String> result1 = client1.call("com.magenta.notification.fetch", String.class, id);
               result1.subscribe(new Action1<String>() {
                  @Override
                  public void call(String t1) {
                     System.out.println("Completed add with result: " + t1);
                  }
               }, new Action1<Throwable>() {
                  @Override
                  public void call(Throwable t1) {
                     System.out.println("Completed add with error: " + t1);
                  }
               });

            }
            if (t1 instanceof WampClient.DisconnectedState) {
               System.out.println("Session1 disconnected");
            }
         }
      }, new Action1<Throwable>() {
         @Override
         public void call(Throwable t) {
            System.out.println("Session1 ended with error " + t);
         }
      }, new Action0() {
         @Override
         public void call() {
            System.out.println("Session1 ended normally");
         }
      });

      waitUntilKeypressed();
      System.out.println("Closing the client 1");
      client1.close().toBlocking().last();
   }

   private void waitUntilKeypressed() {
      try {
         System.in.read();
         while (System.in.available() > 0) {
            System.in.read();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
