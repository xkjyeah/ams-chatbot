package sg.com.ambulanceservice.chatbot.odm

import org.bson.BsonValue

class ConvertToBson[T](val serialize: Function[T, BsonValue], val deserialize: Function[BsonValue, T])
