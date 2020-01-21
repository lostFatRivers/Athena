// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Enums.proto

package com.jokerbee.protocol;

public final class Enums {
  private Enums() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * <pre>
   * 消息类型id
   * </pre>
   *
   * Protobuf enum {@code aft.MessageType}
   */
  public enum MessageType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     * 无
     * </pre>
     *
     * <code>NONE = 0;</code>
     */
    NONE(0),
    /**
     * <pre>
     * 玩家进入.
     * </pre>
     *
     * <code>CS_PLAYER_ENTER = 10001;</code>
     */
    CS_PLAYER_ENTER(10001),
    /**
     * <code>SC_PLAYER_ENTER = 10002;</code>
     */
    SC_PLAYER_ENTER(10002),
    /**
     * <pre>
     * 玩家选择匹配.
     * </pre>
     *
     * <code>CS_GAME_MATCH = 10003;</code>
     */
    CS_GAME_MATCH(10003),
    /**
     * <code>SC_GAME_MATCH = 10004;</code>
     */
    SC_GAME_MATCH(10004),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     * 无
     * </pre>
     *
     * <code>NONE = 0;</code>
     */
    public static final int NONE_VALUE = 0;
    /**
     * <pre>
     * 玩家进入.
     * </pre>
     *
     * <code>CS_PLAYER_ENTER = 10001;</code>
     */
    public static final int CS_PLAYER_ENTER_VALUE = 10001;
    /**
     * <code>SC_PLAYER_ENTER = 10002;</code>
     */
    public static final int SC_PLAYER_ENTER_VALUE = 10002;
    /**
     * <pre>
     * 玩家选择匹配.
     * </pre>
     *
     * <code>CS_GAME_MATCH = 10003;</code>
     */
    public static final int CS_GAME_MATCH_VALUE = 10003;
    /**
     * <code>SC_GAME_MATCH = 10004;</code>
     */
    public static final int SC_GAME_MATCH_VALUE = 10004;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static MessageType valueOf(int value) {
      return forNumber(value);
    }

    public static MessageType forNumber(int value) {
      switch (value) {
        case 0: return NONE;
        case 10001: return CS_PLAYER_ENTER;
        case 10002: return SC_PLAYER_ENTER;
        case 10003: return CS_GAME_MATCH;
        case 10004: return SC_GAME_MATCH;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MessageType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        MessageType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<MessageType>() {
            public MessageType findValueByNumber(int number) {
              return MessageType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.jokerbee.protocol.Enums.getDescriptor().getEnumTypes().get(0);
    }

    private static final MessageType[] VALUES = values();

    public static MessageType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private MessageType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:aft.MessageType)
  }


  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\013Enums.proto\022\003aft*k\n\013MessageType\022\010\n\004NON" +
      "E\020\000\022\024\n\017CS_PLAYER_ENTER\020\221N\022\024\n\017SC_PLAYER_E" +
      "NTER\020\222N\022\022\n\rCS_GAME_MATCH\020\223N\022\022\n\rSC_GAME_M" +
      "ATCH\020\224NB\036\n\025com.jokerbee.protocolB\005Enumsb" +
      "\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}