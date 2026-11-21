package com.nexus.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private val Navy = Color(0xFF101B2D)
private val Blue = Color(0xFF3978F6)
private val Page = Color(0xFFF3F6FB)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NexusApp() } }
}
data class Person(val uid:String,val username:String,val email:String="")
data class ChatMessage(val id:String,val sender:String,val text:String,val time:Long=System.currentTimeMillis())

@Composable
fun NexusApp() {
 val context = LocalContext.current
 val configured = remember { FirebaseApp.getApps(context).isNotEmpty() }
 val auth = remember(configured) { if(configured) FirebaseAuth.getInstance() else null }
 val db = remember(configured) { if(configured) FirebaseFirestore.getInstance() else null }
 var current by remember { mutableStateOf<Person?>(null) }
 var demo by remember { mutableStateOf(false) }
 var error by remember { mutableStateOf("") }
 var loading by remember { mutableStateOf(false) }
 MaterialTheme(colorScheme=lightColorScheme(primary=Blue,background=Page)) {
  Surface(Modifier.fillMaxSize(),color=Page) {
   if(current==null && !demo) {
    AuthScreen(configured,loading,error,onAuth={ email,password,username,create ->
     error=""; loading=true
     if(!configured || auth==null || db==null) { loading=false; error="Firebase не настроен. Используй демо-режим." }
     else if(create) {
      auth.createUserWithEmailAndPassword(email.trim(),password).addOnSuccessListener { result ->
       val u=result.user
       if(u==null){loading=false;error="Не удалось создать аккаунт"} else {
        val name=username.trim().removePrefix("@").lowercase()
        db.collection("users").document(u.uid).set(mapOf("uid" to u.uid,"username" to name,"email" to email.trim()))
         .addOnSuccessListener { current=Person(u.uid,name,email);loading=false }
         .addOnFailureListener { error=it.localizedMessage ?: "Ошибка профиля";loading=false }
       }
      }.addOnFailureListener { error=it.localizedMessage ?: "Ошибка регистрации";loading=false }
     } else {
      auth.signInWithEmailAndPassword(email.trim(),password).addOnSuccessListener { result ->
       val u=result.user
       if(u==null){loading=false;error="Аккаунт не найден"} else db.collection("users").document(u.uid).get()
        .addOnSuccessListener { d -> current=Person(u.uid,d.getString("username") ?: email.substringBefore("@"),email);loading=false }
        .addOnFailureListener { error=it.localizedMessage ?: "Ошибка профиля";loading=false }
      }.addOnFailureListener { error=it.localizedMessage ?: "Ошибка входа";loading=false }
     }
    },onDemo={demo=true;current=Person("demo-user","guest","guest@nexus.local")})
   } else if(current!=null) MessengerScreen(current!!,db,demo || !configured,onLogout={if(!demo)auth?.signOut();current=null;demo=false})
  }
 }
}

@Composable
private fun AuthScreen(configured:Boolean,loading:Boolean,error:String,onAuth:(String,String,String,Boolean)->Unit,onDemo:()->Unit) {
 var create by remember { mutableStateOf(false) }
 var email by remember { mutableStateOf("") }
 var password by remember { mutableStateOf("") }
 var username by remember { mutableStateOf("") }
 Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center) {
  Text("NEXUS",color=Navy,fontSize=38.sp,fontWeight=FontWeight.Black)
  Text("MESSENGER",color=Blue,fontSize=13.sp,fontWeight=FontWeight.Bold,letterSpacing=3.sp)
  Spacer(Modifier.height(28.dp))
  Text(if(create)"Создать аккаунт" else "С возвращением",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Navy)
  Text("Общайтесь просто. Будьте на связи.",color=Color.Gray)
  Spacer(Modifier.height(22.dp))
  if(create) OutlinedTextField(username,{username=it},Modifier.fillMaxWidth(),label={Text("Username")},singleLine=true,placeholder={Text("например, alex")})
  OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Электронная почта")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email))
  OutlinedTextField(password,{password=it},Modifier.fillMaxWidth(),label={Text("Пароль")},singleLine=true,visualTransformation=PasswordVisualTransformation())
  if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error,fontSize=13.sp)
  Spacer(Modifier.height(14.dp))
  Button(onClick={onAuth(email,password,username,create)},Modifier.fillMaxWidth().height(52.dp),enabled=!loading&&email.isNotBlank()&&password.length>=6&&(!create||username.length>=3)){Text(if(loading)"Подождите…" else if(create)"Зарегистрироваться" else "Войти")}
  TextButton(onClick={create=!create},Modifier.align(Alignment.CenterHorizontally)){Text(if(create)"Уже есть аккаунт? Войти" else "Создать новый аккаунт")}
  HorizontalDivider(Modifier.padding(vertical=8.dp))
  OutlinedButton(onClick=onDemo,Modifier.fillMaxWidth()){Text("Попробовать демо без регистрации")}
  Text(if(configured)"Firebase подключён" else "Для общих аккаунтов и чатов между телефонами настрой Firebase.",color=Color.Gray,fontSize=11.sp,modifier=Modifier.padding(top=12.dp))
 }
}

@Composable
private fun MessengerScreen(me:Person,db:FirebaseFirestore?,local:Boolean,onLogout:()->Unit) {
 var selected by remember { mutableStateOf<Person?>(null) }
 var search by remember { mutableStateOf("") }
 var people by remember { mutableStateOf(listOf<Person>()) }
 var chats by remember { mutableStateOf(listOf<Person>()) }
 var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
 var draft by remember { mutableStateOf("") }
 val demoMessages=remember { mutableStateListOf<ChatMessage>() }
 var messageListener by remember { mutableStateOf<ListenerRegistration?>(null) }
 DisposableEffect(selected?.uid,local) {
  messageListener?.remove()
  if(!local&&db!=null&&selected!=null) {
   val chatId=listOf(me.uid,selected!!.uid).sorted().joinToString("_")
   messageListener=db.collection("chats").document(chatId).collection("messages").orderBy("time")
    .addSnapshotListener { snap,_ -> messages=snap?.documents?.map { ChatMessage(it.id,it.getString("sender") ?: "",it.getString("text") ?: "",it.getLong("time") ?: 0L) } ?: emptyList() }
  } else messages=demoMessages.toList()
  onDispose { messageListener?.remove() }
 }
 LaunchedEffect(search,local) {
  if(!local&&db!=null&&search.trim().length>=2) {
   kotlinx.coroutines.delay(350)
   val q=search.trim().removePrefix("@").lowercase()
   db.collection("users").orderBy("username").startAt(q).endAt(q+"\uf8ff").limit(20).get()
    .addOnSuccessListener { r -> people=r.documents.filter{it.id!=me.uid}.map{Person(it.id,it.getString("username") ?: "user",it.getString("email") ?: "")} }
  } else if(local&&search.isNotBlank()) people=listOf(Person("demo-friend","nexus_friend"))
  else people=emptyList()
 }
 Column(Modifier.fillMaxSize().background(Page)) {
  Row(Modifier.fillMaxWidth().background(Navy).padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
   if(selected!=null) IconButton(onClick={selected=null}){Icon(Icons.Default.ArrowBack,null,tint=Color.White)}
   Column(Modifier.weight(1f)){Text(if(selected==null)"NEXUS" else "@"+selected!!.username,color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(if(local)"ДЕМО-РЕЖИМ" else "В СЕТИ",color=Color(0xFFB9C8E4),fontSize=11.sp)}
   if(selected==null) TextButton(onClick=onLogout){Text("Выйти",color=Color.White)}
  }
  if(selected==null) {
   OutlinedTextField(search,{search=it},Modifier.fillMaxWidth().padding(14.dp),placeholder={Text("Найти по username")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true)
   val shown=if(search.isBlank())chats else people
   if(shown.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(if(search.isBlank())"Пока нет чатов\nНайди пользователя по username" else "Пользователи не найдены",color=Color.Gray)}
   else LazyColumn { items(shown,key={it.uid}) { person ->
    Row(Modifier.fillMaxWidth().clickable {
     selected=person
     if(!local&&db!=null) { val id=listOf(me.uid,person.uid).sorted().joinToString("_");db.collection("chats").document(id).set(mapOf("participants" to listOf(me.uid,person.uid),"updatedAt" to System.currentTimeMillis()));chats=(chats+person).distinctBy{it.uid} }
    }.padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
     Box(Modifier.size(48.dp).background(Blue,CircleShape),contentAlignment=Alignment.Center){Text(person.username.take(1).uppercase(),color=Color.White,fontWeight=FontWeight.Bold)}
     Column(Modifier.padding(start=12.dp)){Text("@"+person.username,fontWeight=FontWeight.SemiBold,color=Navy);Text("Открыть переписку",color=Color.Gray,fontSize=12.sp)}
    };HorizontalDivider(color=Color(0xFFE5EAF2))
   } }
  } else {
   LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(12.dp)) { items(messages,key={it.id}) { msg ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=if(msg.sender==me.uid)Arrangement.End else Arrangement.Start) {
     Surface(color=if(msg.sender==me.uid)Color(0xFFDCE8FF) else Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.padding(vertical=4.dp)){Text(msg.text,Modifier.padding(horizontal=14.dp,vertical=10.dp),color=Navy)}
    }
   } }
   Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp),verticalAlignment=Alignment.CenterVertically) {
    OutlinedTextField(draft,{draft=it},Modifier.weight(1f),placeholder={Text("Сообщение…")},maxLines=4)
    IconButton(onClick={
     val value=draft.trim()
     if(value.isNotEmpty()) {
      if(local||db==null){demoMessages.add(ChatMessage("m"+demoMessages.size,me.uid,value));messages=demoMessages.toList()}
      else {val id=listOf(me.uid,selected!!.uid).sorted().joinToString("_");db.collection("chats").document(id).collection("messages").add(mapOf("sender" to me.uid,"text" to value,"time" to System.currentTimeMillis()));db.collection("chats").document(id).update("updatedAt",System.currentTimeMillis())}
      draft=""
     }
    }){Icon(Icons.Default.Send,"Отправить",tint=Blue)}
   }
  }
 }
}
