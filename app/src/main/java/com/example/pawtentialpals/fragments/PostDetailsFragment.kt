package com.example.pawtentialpals.fragments

import ImageSliderAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.FragmentPostDetailsBinding
import com.example.pawtentialpals.models.Comment
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var post: PostModel

    private val args: PostDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                val fetchedPost = document.toObject(PostModel::class.java)
                fetchedPost?.let {
                    post = it
                    displayPostDetails()
                    loadComments()
                }
            }

        binding.commentButton.setOnClickListener {
            val newCommentText = binding.commentInput.text.toString()
            if (newCommentText.isNotEmpty()) {
                addComment(newCommentText)
                binding.commentInput.text.clear()
            }
        }
    }

    private fun displayPostDetails() {
        binding.username.text = post.userName
        binding.postTime.text = formatTimestamp(post.timestamp)
        binding.postContent.text = post.description
        binding.postLocation.text = post.location
        binding.userImage.load(post.userImage) {
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
        }

        val imageUrls = listOf(post.postImage, post.mapImage)
        binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)
    }

    private fun addComment(commentText: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Unknown User"
                val userImage = document.getString("image") ?: ""

                val newComment = Comment(
                    userId = userId,
                    userName = userName,
                    userImage = userImage,
                    comment = commentText,
                    timestamp = System.currentTimeMillis()
                )

                post.comments += newComment

                firestore.collection("posts").document(post.id).update("comments", post.comments)
                    .addOnSuccessListener {
                        displayComment(newComment)
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
            }
    }

    private fun loadComments() {
        firestore.collection("posts").document(post.id).get()
            .addOnSuccessListener { document ->
                val fetchedPost = document.toObject(PostModel::class.java)
                fetchedPost?.comments?.forEach { comment ->
                    displayComment(comment)
                }
            }
    }

    private fun displayComment(comment: Comment) {
        val commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, binding.commentsContainer, false)
        val userImage = commentView.findViewById<ImageView>(R.id.comment_user_image)
        val userName = commentView.findViewById<TextView>(R.id.comment_user_name)
        val commentText = commentView.findViewById<TextView>(R.id.comment_text)
        val commentTime = commentView.findViewById<TextView>(R.id.comment_time)

        userImage.load(comment.userImage) {
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
        }
        userName.text = comment.userName
        commentText.text = comment.comment
        commentTime.text = formatTimestamp(comment.timestamp)

        binding.commentsContainer.addView(commentView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}