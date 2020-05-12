package com.juniormargalho.olx.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.juniormargalho.olx.R;
import com.juniormargalho.olx.helper.ConfiguracaoFirebase;

public class MainActivity extends AppCompatActivity {
    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializaComponentes();

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if(!senha.isEmpty()){

                        //verifica estado do switch
                        if(tipoAcesso.isChecked()){

                            //cadastro
                            autenticacao.createUserWithEmailAndPassword(email,senha).addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(MainActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                            }else {
                                                String excecao = "";
                                                try {
                                                    throw task.getException();
                                                }catch (FirebaseAuthWeakPasswordException e){
                                                    excecao = "Digite uma senha mais forte!";
                                                }catch (FirebaseAuthInvalidCredentialsException e){
                                                    excecao = "Por Favor, digite um E-mail válido!";
                                                }catch (FirebaseAuthUserCollisionException e){
                                                    excecao = "Esta conta já foi cadastrada!";
                                                }catch (Exception e){
                                                    excecao = "Erro ao cadastrar Usuário: " + e.getMessage();
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(MainActivity.this, "Erro: " + excecao, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                            //fim do cadastro

                        }else {

                            //login
                            autenticacao.signInWithEmailAndPassword(email, senha).addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(MainActivity.this, "Logado com sucesso!", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(MainActivity.this, "Erro ao fazer login: " + task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                            //fim do login

                        }
                    }else {
                        Toast.makeText(MainActivity.this, "Preencha a senha, por favor!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Preencha o E-mail, por favor!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializaComponentes(){
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
    }

}
