import com.yf.Robot;
import com.yf.common.extension.ExtensionLoader;
import com.yf.common.factory.SingletonFactory;
import com.yf.compress.Compress;
import com.yf.compress.gzip.GzipCompress;
import com.yf.config.RpcServiceConfig;
import com.yf.loadbalance.balancer.ConsistentHashLoadBalance;
import com.yf.proxy.RpcClientProxy;
import com.yf.registry.ServiceDiscovery;
import com.yf.registry.ServiceRegistry;
import com.yf.registry.zk.ServiceDiscoveryImpl;
import com.yf.registry.zk.ServiceRegisterImpl;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;
import com.yf.serialize.Serializer;
import com.yf.serialize.kyro.KyroSerializer;
import com.yf.service.HelloService;
import com.yf.service.impl.HelloServiceImpl;
import com.yf.transport.RpcRequestTransport;
import com.yf.transport.client.NettyRpcClient;
import com.yf.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 15:38
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class test {

    @Test
    public void testSerialize(){
//        RpcRequest request = RpcRequest.builder()
//                .requestId("12306")
//                .methodName("test method")
//                .build();
//
//        KyroSerializer kyroSerializer = new KyroSerializer();
//        byte[] serialize = kyroSerializer.serialize(request);
//        System.out.println(serialize);
//
//        KyroSerializer kyroSerializer1 = new KyroSerializer();
//        Object deserialize = kyroSerializer1.deserialize(serialize,RpcRequest.class);
//        System.out.println(deserialize);

        RpcResponse<Object> abc = RpcResponse.success(null, "abc");
        Serializer serializer = new KyroSerializer();
        byte[] serialize = serializer.serialize(abc);
        Serializer serializer1 = new KyroSerializer();
        RpcResponse deserialize = serializer1.deserialize(serialize, RpcResponse.class);
        System.out.println(deserialize);

    }

    @Test
    public void testCurator() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString("192.168.64.129")
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        zkClient.create().forPath("/node2","java".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = zkClient.getData().forPath("/node2");
        System.out.println(bytes);
    }

    @Test
    public void testRegistry(){
        RpcRequest request = RpcRequest.builder()
                .methodName("myMethod")
                .interfaceName("myInterface")
                .group("myGroup")
                .build();

        ServiceRegistry serviceRegistry = new ServiceRegisterImpl();
        serviceRegistry.registerService(new InetSocketAddress("192.168.64.129",7777),request.getRpcServiceName());


        ServiceDiscovery serviceDiscovery = new ServiceDiscoveryImpl();


        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(request);
        System.out.println(inetSocketAddress);
    }

    @Test
    public void testZip() throws IOException {
        Compress compress = new GzipCompress();
        byte[] bytes = "javaabchahahahhahahasdjfhsjkhncoscooasnflvnalbfhacjklnaklnscklnlajkbjklcbjkab vmls dlvbnljashbvlakl;vc#^#%&$fvhuihinadfllhahhahahahahha".getBytes();
        System.out.println(bytes.length);

        byte[] compressd = compress.compress(bytes);
        System.out.println(compressd.length);
        System.out.println(compressd);
        byte[] decompress = compress.decompress(compressd);
        System.out.println(new String(decompress));


    }

    @Test
    public void testServerRegiste(){
        HelloService helloService = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test1")
                .version("version1")
                .service(helloService)
                .token("token-a")
                .build();
        // 手工注册服务

        NettyRpcServer server = SingletonFactory.getInstance(NettyRpcServer.class);
        server.registerService(rpcServiceConfig);
        try {
            server.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testClientDiscovery() throws IOException {
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setGroup("test1");
        rpcServiceConfig.setVersion("version1");
        rpcServiceConfig.setToken("token-a");
        RpcRequestTransport rpcRequestTransport = SingletonFactory.getInstance(NettyRpcClient.class);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcServiceConfig,rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        helloService.sayHello();
        System.out.println("yyyyy");
        System.in.read();

    }

    @Test
    public void testSPI(){
        ServiceLoader<Robot> load = ServiceLoader.load(Robot.class);
        load.forEach(Robot::sayHello);
    }

    @Test
    public void testFannsySPI(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer kyro = extensionLoader.getExtension("kyro");
        System.out.println(kyro);

    }

    @Test
    public void testBalance(){
        List<String> serviceUrlList = new ArrayList<>();
        serviceUrlList.add("192.168.64.129:100");
        serviceUrlList.add("192.168.64.130:100");
        serviceUrlList.add("192.168.64.131:100");
        serviceUrlList.add("192.168.64.132:100");
        serviceUrlList.add("192.168.64.133:100");
        System.out.println(serviceUrlList);
        HelloService helloService = new HelloServiceImpl();
        String param = "ac";
        RpcRequest rpcRequest = RpcRequest.builder().requestId("abc").parameters(new Object[]{param}).methodName("helloService").build();

        ConsistentHashLoadBalance consistentHashLoadBalance = new ConsistentHashLoadBalance();
        String s = consistentHashLoadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        System.out.println(s);



    }



}
