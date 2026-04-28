package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var enderecoEditText: EditText
    private lateinit var nomeEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var itemImageView: ImageView
    private lateinit var salvarButton: Button
    private lateinit var selectImageButton: Button

    private var imageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inicializando os componentes do layout
        itemImageView = binding.imageItem
        salvarButton = binding.salvarItemButton
        selectImageButton = binding.buttonSelectImage
        enderecoEditText = binding.enderecoItemEditText
        nomeEditText = binding.nomeItemEditText
        descricaoEditText = binding.descricaoItemEditText

        auth = FirebaseAuth.getInstance()

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        salvarButton.setOnClickListener {
            salvarItem()
        }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun salvarItem() {
        val endereco = enderecoEditText.text.toString().trim()
        val nome = nomeEditText.text.toString().trim()
        val descricao = descricaoEditText.text.toString().trim()

        if (endereco.isEmpty() || nome.isEmpty() || descricao.isEmpty() || imageUri == null) {
            Toast.makeText(context, "Por favor, preencha todos os campos e a foto", Toast.LENGTH_SHORT).show()
            return
        }
        uploadImageToFirestore()
    }

    private fun uploadImageToFirestore() {
        if (imageUri != null) {
            try {
                val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                // ... dentro do uploadImageToFirestore ...
                if (bytes != null) {
                    val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                    val enderecoText = enderecoEditText.text.toString().trim()
                    val nomeText = nomeEditText.text.toString().trim()
                    val descricaoText = descricaoEditText.text.toString().trim()

                    // Use "item" com 'i' minúsculo para a variável
                    val item = Item(enderecoText,
                        base64Image,
                        null,
                        null,
                        nomeText,
                        descricaoText,
                        auth.uid.toString()
                    )

                    saveItemIntoDatabase(item)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(itemImageView)
        }
    }

    private fun saveItemIntoDatabase(item: Item) {
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")
        val itemId = databaseReference.push().key

        if (itemId != null) {
            databaseReference.child(auth.uid.toString()).child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }.addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}