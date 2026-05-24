package com.t2h.ocr.data.auth

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        auth = mockk(relaxed = true)
        every { FirebaseAuth.getInstance() } returns auth
        repository = AuthRepository(auth)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getUid returns current user uid`() {
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "test-uid"
        every { auth.currentUser } returns mockUser

        assertEquals("test-uid", repository.getUid())
    }

    @Test
    fun `signInAnonymously does nothing if user already signed in`() {
        val mockUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns mockUser
        
        var called = false
        repository.signInAnonymously { success ->
            called = success
        }

        verify(exactly = 0) { auth.signInAnonymously() }
        assertEquals(true, called)
    }

    @Test
    fun `signInAnonymously calls onComplete true when successful`() {
        every { auth.currentUser } returns null
        val mockTask = mockk<Task<AuthResult>>()
        val listenerSlot = slot<OnCompleteListener<AuthResult>>()
        
        every { auth.signInAnonymously() } returns mockTask
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } returns mockTask
        every { mockTask.isSuccessful } returns true

        var result: Boolean? = null
        repository.signInAnonymously { success ->
            result = success
        }

        listenerSlot.captured.onComplete(mockTask)
        assertEquals(true, result)
    }

    @Test
    fun `linkWithGoogle returns false if no user logged in`() {
        every { auth.currentUser } returns null
        
        var result: Boolean? = null
        repository.linkWithGoogle("dummy-token") { success ->
            result = success
        }
        
        assertEquals(false, result)
    }

    @Test
    fun `linkWithGoogle calls onComplete true when successful`() {
        val mockUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns mockUser
        
        val mockTask = mockk<Task<AuthResult>>()
        val listenerSlot = slot<OnCompleteListener<AuthResult>>()
        
        mockkStatic(GoogleAuthProvider::class)
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockk()
        
        every { mockUser.linkWithCredential(any()) } returns mockTask
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } returns mockTask
        every { mockTask.isSuccessful } returns true

        var result: Boolean? = null
        repository.linkWithGoogle("dummy-token") { success ->
            result = success
        }

        listenerSlot.captured.onComplete(mockTask)
        assertEquals(true, result)
    }

    @Test
    fun `linkWithGoogle calls onComplete false when failed`() {
        val mockUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns mockUser
        
        val mockTask = mockk<Task<AuthResult>>()
        val listenerSlot = slot<OnCompleteListener<AuthResult>>()
        
        mockkStatic(GoogleAuthProvider::class)
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockk()
        
        every { mockUser.linkWithCredential(any()) } returns mockTask
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } returns mockTask
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns Exception("link error")

        var result: Boolean? = null
        repository.linkWithGoogle("dummy-token") { success ->
            result = success
        }

        listenerSlot.captured.onComplete(mockTask)
        assertEquals(false, result)
    }
}
