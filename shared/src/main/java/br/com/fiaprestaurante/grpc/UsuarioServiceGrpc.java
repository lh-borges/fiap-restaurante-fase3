package br.com.fiaprestaurante.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
 * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: usuario.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UsuarioServiceGrpc {

  private UsuarioServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "fiaprestaurante.UsuarioService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.BuscarUsuarioRequest,
      br.com.fiaprestaurante.grpc.UsuarioResponse> getBuscarUsuarioMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BuscarUsuario",
      requestType = br.com.fiaprestaurante.grpc.BuscarUsuarioRequest.class,
      responseType = br.com.fiaprestaurante.grpc.UsuarioResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.BuscarUsuarioRequest,
      br.com.fiaprestaurante.grpc.UsuarioResponse> getBuscarUsuarioMethod() {
    io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.BuscarUsuarioRequest, br.com.fiaprestaurante.grpc.UsuarioResponse> getBuscarUsuarioMethod;
    if ((getBuscarUsuarioMethod = UsuarioServiceGrpc.getBuscarUsuarioMethod) == null) {
      synchronized (UsuarioServiceGrpc.class) {
        if ((getBuscarUsuarioMethod = UsuarioServiceGrpc.getBuscarUsuarioMethod) == null) {
          UsuarioServiceGrpc.getBuscarUsuarioMethod = getBuscarUsuarioMethod =
              io.grpc.MethodDescriptor.<br.com.fiaprestaurante.grpc.BuscarUsuarioRequest, br.com.fiaprestaurante.grpc.UsuarioResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BuscarUsuario"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.com.fiaprestaurante.grpc.BuscarUsuarioRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.com.fiaprestaurante.grpc.UsuarioResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UsuarioServiceMethodDescriptorSupplier("BuscarUsuario"))
              .build();
        }
      }
    }
    return getBuscarUsuarioMethod;
  }

  private static volatile io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.VerificarPerfilRequest,
      br.com.fiaprestaurante.grpc.VerificarPerfilResponse> getVerificarPerfilMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "VerificarPerfil",
      requestType = br.com.fiaprestaurante.grpc.VerificarPerfilRequest.class,
      responseType = br.com.fiaprestaurante.grpc.VerificarPerfilResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.VerificarPerfilRequest,
      br.com.fiaprestaurante.grpc.VerificarPerfilResponse> getVerificarPerfilMethod() {
    io.grpc.MethodDescriptor<br.com.fiaprestaurante.grpc.VerificarPerfilRequest, br.com.fiaprestaurante.grpc.VerificarPerfilResponse> getVerificarPerfilMethod;
    if ((getVerificarPerfilMethod = UsuarioServiceGrpc.getVerificarPerfilMethod) == null) {
      synchronized (UsuarioServiceGrpc.class) {
        if ((getVerificarPerfilMethod = UsuarioServiceGrpc.getVerificarPerfilMethod) == null) {
          UsuarioServiceGrpc.getVerificarPerfilMethod = getVerificarPerfilMethod =
              io.grpc.MethodDescriptor.<br.com.fiaprestaurante.grpc.VerificarPerfilRequest, br.com.fiaprestaurante.grpc.VerificarPerfilResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "VerificarPerfil"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.com.fiaprestaurante.grpc.VerificarPerfilRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.com.fiaprestaurante.grpc.VerificarPerfilResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UsuarioServiceMethodDescriptorSupplier("VerificarPerfil"))
              .build();
        }
      }
    }
    return getVerificarPerfilMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UsuarioServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceStub>() {
        @java.lang.Override
        public UsuarioServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UsuarioServiceStub(channel, callOptions);
        }
      };
    return UsuarioServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UsuarioServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceBlockingStub>() {
        @java.lang.Override
        public UsuarioServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UsuarioServiceBlockingStub(channel, callOptions);
        }
      };
    return UsuarioServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UsuarioServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UsuarioServiceFutureStub>() {
        @java.lang.Override
        public UsuarioServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UsuarioServiceFutureStub(channel, callOptions);
        }
      };
    return UsuarioServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
   * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Busca os dados de um usuário pelo seu identificador público
     * </pre>
     */
    default void buscarUsuario(br.com.fiaprestaurante.grpc.BuscarUsuarioRequest request,
        io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.UsuarioResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBuscarUsuarioMethod(), responseObserver);
    }

    /**
     * <pre>
     * Verifica se um usuário possui o perfil necessário para uma operação
     * </pre>
     */
    default void verificarPerfil(br.com.fiaprestaurante.grpc.VerificarPerfilRequest request,
        io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.VerificarPerfilResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getVerificarPerfilMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UsuarioService.
   * <pre>
   * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
   * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
   * </pre>
   */
  public static abstract class UsuarioServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UsuarioServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UsuarioService.
   * <pre>
   * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
   * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
   * </pre>
   */
  public static final class UsuarioServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UsuarioServiceStub> {
    private UsuarioServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UsuarioServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UsuarioServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Busca os dados de um usuário pelo seu identificador público
     * </pre>
     */
    public void buscarUsuario(br.com.fiaprestaurante.grpc.BuscarUsuarioRequest request,
        io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.UsuarioResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBuscarUsuarioMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Verifica se um usuário possui o perfil necessário para uma operação
     * </pre>
     */
    public void verificarPerfil(br.com.fiaprestaurante.grpc.VerificarPerfilRequest request,
        io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.VerificarPerfilResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getVerificarPerfilMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UsuarioService.
   * <pre>
   * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
   * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
   * </pre>
   */
  public static final class UsuarioServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UsuarioServiceBlockingStub> {
    private UsuarioServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UsuarioServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UsuarioServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Busca os dados de um usuário pelo seu identificador público
     * </pre>
     */
    public br.com.fiaprestaurante.grpc.UsuarioResponse buscarUsuario(br.com.fiaprestaurante.grpc.BuscarUsuarioRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBuscarUsuarioMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Verifica se um usuário possui o perfil necessário para uma operação
     * </pre>
     */
    public br.com.fiaprestaurante.grpc.VerificarPerfilResponse verificarPerfil(br.com.fiaprestaurante.grpc.VerificarPerfilRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getVerificarPerfilMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UsuarioService.
   * <pre>
   * Serviço gRPC para comunicação entre microsserviços que precisam consultar dados de usuários.
   * Utilizado pelos módulos de pagamento e restaurante/pedido para verificar permissões.
   * </pre>
   */
  public static final class UsuarioServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UsuarioServiceFutureStub> {
    private UsuarioServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UsuarioServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UsuarioServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Busca os dados de um usuário pelo seu identificador público
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<br.com.fiaprestaurante.grpc.UsuarioResponse> buscarUsuario(
        br.com.fiaprestaurante.grpc.BuscarUsuarioRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBuscarUsuarioMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Verifica se um usuário possui o perfil necessário para uma operação
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<br.com.fiaprestaurante.grpc.VerificarPerfilResponse> verificarPerfil(
        br.com.fiaprestaurante.grpc.VerificarPerfilRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getVerificarPerfilMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_BUSCAR_USUARIO = 0;
  private static final int METHODID_VERIFICAR_PERFIL = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_BUSCAR_USUARIO:
          serviceImpl.buscarUsuario((br.com.fiaprestaurante.grpc.BuscarUsuarioRequest) request,
              (io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.UsuarioResponse>) responseObserver);
          break;
        case METHODID_VERIFICAR_PERFIL:
          serviceImpl.verificarPerfil((br.com.fiaprestaurante.grpc.VerificarPerfilRequest) request,
              (io.grpc.stub.StreamObserver<br.com.fiaprestaurante.grpc.VerificarPerfilResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getBuscarUsuarioMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              br.com.fiaprestaurante.grpc.BuscarUsuarioRequest,
              br.com.fiaprestaurante.grpc.UsuarioResponse>(
                service, METHODID_BUSCAR_USUARIO)))
        .addMethod(
          getVerificarPerfilMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              br.com.fiaprestaurante.grpc.VerificarPerfilRequest,
              br.com.fiaprestaurante.grpc.VerificarPerfilResponse>(
                service, METHODID_VERIFICAR_PERFIL)))
        .build();
  }

  private static abstract class UsuarioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UsuarioServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return br.com.fiaprestaurante.grpc.UsuarioProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UsuarioService");
    }
  }

  private static final class UsuarioServiceFileDescriptorSupplier
      extends UsuarioServiceBaseDescriptorSupplier {
    UsuarioServiceFileDescriptorSupplier() {}
  }

  private static final class UsuarioServiceMethodDescriptorSupplier
      extends UsuarioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UsuarioServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UsuarioServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UsuarioServiceFileDescriptorSupplier())
              .addMethod(getBuscarUsuarioMethod())
              .addMethod(getVerificarPerfilMethod())
              .build();
        }
      }
    }
    return result;
  }
}
